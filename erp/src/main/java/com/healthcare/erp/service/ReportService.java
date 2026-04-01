package com.healthcare.erp.service;

import com.healthcare.erp.dto.report.*;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final StaffRepository staffRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    /**
     * Single overview of a hospital's key operating metrics.
     */
    public DashboardSummaryDTO getDashboardSummary(UUID hospitalId) {
        validateHospital(hospitalId);

        long totalPatients = patientRepository.countByHospitalId(hospitalId);
        long activePatients = patientRepository.countByHospitalIdAndIsActiveTrue(hospitalId);
        long totalDoctors = doctorRepository.countByHospitalIdAndIsActiveTrue(hospitalId);
        long totalStaff = staffRepository.countByHospitalIdAndIsActiveTrue(hospitalId);
        long totalAppointments = appointmentRepository.countByHospitalId(hospitalId);
        long pending = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.SCHEDULED);
        long completed = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.COMPLETED);
        long cancelled = appointmentRepository.countByHospitalIdAndStatus(hospitalId, AppointmentStatus.CANCELLED);

        BigDecimal totalRevenue = invoiceRepository.sumTotalAmountByHospitalId(hospitalId);
        BigDecimal totalPaid = invoiceRepository.sumPaidAmountByHospitalId(hospitalId);
        BigDecimal outstanding = totalRevenue.subtract(totalPaid);

        long lowStock = medicineRepository.findByHospitalIdAndStockQuantityLessThanEqual(hospitalId, 10).size();
        long totalPrescriptions = prescriptionRepository.countByHospitalId(hospitalId);
        long totalInvoices = invoiceRepository.countByHospitalId(hospitalId);

        auditService.logRead("Report", "DASHBOARD", hospitalId, null);

        return new DashboardSummaryDTO(
                totalPatients, activePatients, totalDoctors, totalStaff,
                totalAppointments, pending, completed, cancelled,
                totalRevenue, totalPaid, outstanding,
                lowStock, totalPrescriptions, totalInvoices
        );
    }

    /**
     * Patient demographics: gender and blood group distribution.
     */
    public PatientStatsDTO getPatientStats(UUID hospitalId) {
        validateHospital(hospitalId);

        long total = patientRepository.countByHospitalId(hospitalId);
        long active = patientRepository.countByHospitalIdAndIsActiveTrue(hospitalId);
        long inactive = patientRepository.countByHospitalIdAndIsActiveFalse(hospitalId);

        Map<String, Long> genderDist = patientRepository.countByHospitalIdGroupByGender(hospitalId)
                .stream().collect(Collectors.toMap(
                        r -> r[0] != null ? r[0].toString() : "UNKNOWN",
                        r -> (Long) r[1],
                        (a, b) -> a, LinkedHashMap::new
                ));

        Map<String, Long> bloodGroupDist = patientRepository.countByHospitalIdGroupByBloodGroup(hospitalId)
                .stream().collect(Collectors.toMap(
                        r -> r[0] != null ? r[0].toString() : "UNKNOWN",
                        r -> (Long) r[1],
                        (a, b) -> a, LinkedHashMap::new
                ));

        auditService.logRead("Report", "PATIENT_STATS", hospitalId, null);

        return new PatientStatsDTO(total, active, inactive, genderDist, bloodGroupDist);
    }

    /**
     * Revenue metrics: invoices, GST, payments, insurance claims.
     */
    public RevenueReportDTO getRevenueReport(UUID hospitalId) {
        validateHospital(hospitalId);

        BigDecimal totalRevenue = invoiceRepository.sumTotalAmountByHospitalId(hospitalId);
        BigDecimal totalGst = invoiceRepository.sumGstAmountByHospitalId(hospitalId);
        BigDecimal totalPaid = invoiceRepository.sumPaidAmountByHospitalId(hospitalId);
        BigDecimal outstanding = totalRevenue.subtract(totalPaid);

        Map<String, Long> invoicesByStatus = new LinkedHashMap<>();
        for (InvoiceStatus status : InvoiceStatus.values()) {
            long count = invoiceRepository.countByHospitalIdAndStatus(hospitalId, status);
            if (count > 0) invoicesByStatus.put(status.name(), count);
        }

        Map<String, Long> claimsByStatus = new LinkedHashMap<>();
        for (ClaimStatus status : ClaimStatus.values()) {
            long count = insuranceClaimRepository.countByHospitalIdAndStatus(hospitalId, status);
            if (count > 0) claimsByStatus.put(status.name(), count);
        }

        BigDecimal totalClaimed = insuranceClaimRepository.sumClaimedAmountByHospitalId(hospitalId);
        BigDecimal totalApproved = insuranceClaimRepository.sumApprovedAmountByHospitalId(hospitalId);

        auditService.logRead("Report", "REVENUE", hospitalId, null);

        return new RevenueReportDTO(
                totalRevenue, totalGst, totalPaid, outstanding,
                invoicesByStatus, claimsByStatus,
                totalClaimed, totalApproved
        );
    }

    /**
     * Per-doctor workload: appointments and prescription counts.
     */
    public List<DoctorWorkloadDTO> getDoctorWorkloads(UUID hospitalId) {
        validateHospital(hospitalId);

        List<Doctor> doctors = doctorRepository.findByHospitalIdAndIsActiveTrue(hospitalId);

        List<DoctorWorkloadDTO> workloads = doctors.stream().map(doc -> {
            long totalAppts = appointmentRepository.countByDoctorIdAndHospitalId(doc.getId(), hospitalId);
            long completedAppts = appointmentRepository.countByDoctorIdAndHospitalIdAndStatus(
                    doc.getId(), hospitalId, AppointmentStatus.COMPLETED);
            long cancelledAppts = appointmentRepository.countByDoctorIdAndHospitalIdAndStatus(
                    doc.getId(), hospitalId, AppointmentStatus.CANCELLED);
            long totalRx = prescriptionRepository.countByDoctorIdAndHospitalId(doc.getId(), hospitalId);

            return new DoctorWorkloadDTO(
                    doc.getId(),
                    doc.getFirstName() + " " + doc.getLastName(),
                    doc.getSpecialization(),
                    totalAppts, completedAppts, cancelledAppts, totalRx
            );
        }).toList();

        auditService.logRead("Report", "DOCTOR_WORKLOAD", hospitalId, null);
        return workloads;
    }

    /**
     * Daily appointment counts for a date range.
     */
    public List<AppointmentTrendDTO> getAppointmentTrends(UUID hospitalId,
                                                           LocalDate startDate, LocalDate endDate) {
        validateHospital(hospitalId);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Object[]> rawResults = appointmentRepository.countByHospitalIdGroupByDate(hospitalId, start, end);

        List<AppointmentTrendDTO> trends = rawResults.stream()
                .map(r -> new AppointmentTrendDTO(
                        (LocalDate) r[0],
                        (Long) r[1]
                ))
                .toList();

        auditService.logRead("Report", "APPOINTMENT_TRENDS", hospitalId, null);
        return trends;
    }

    private void validateHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
    }
}
