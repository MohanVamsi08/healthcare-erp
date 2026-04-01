package com.healthcare.erp.service;

import com.healthcare.erp.dto.PrescriptionDTO;
import com.healthcare.erp.dto.PrescriptionItemDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final MedicineService medicineService;
    private final MedicineRepository medicineRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditService auditService;

    public List<PrescriptionDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("Prescription", "LIST", hospitalId, null);
        return prescriptionRepository.findByHospitalId(hospitalId).stream()
                .map(PrescriptionDTO::fromEntity).toList();
    }

    public PrescriptionDTO getById(UUID hospitalId, UUID id) {
        Prescription rx = getRxWithTenantCheck(hospitalId, id);
        auditService.logRead("Prescription", id.toString(), hospitalId, null);
        return PrescriptionDTO.fromEntity(rx);
    }

    public PrescriptionDTO create(UUID hospitalId, PrescriptionDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Patient does not belong to this hospital");

        // Resolve the doctor from the authenticated user context
        Doctor doctor = resolveDoctor(hospitalId, dto.doctorId());

        if (dto.items() == null || dto.items().isEmpty())
            throw new IllegalArgumentException("At least one prescription item is required");

        String rxNumber = "RX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Prescription rx = Prescription.builder()
                .prescriptionNumber(rxNumber)
                .patient(patient).doctor(doctor).hospital(hospital)
                .notes(dto.notes())
                .build();

        for (PrescriptionItemDTO itemDto : dto.items()) {
            Medicine med = medicineService.getMedicineWithTenantCheck(hospitalId, itemDto.medicineId());

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(rx)
                    .medicine(med)
                    .quantity(itemDto.quantity())
                    .dosageInstructions(itemDto.dosageInstructions())
                    .build();
            rx.getItems().add(item);
        }

        Prescription saved = prescriptionRepository.save(rx);
        auditService.logCreate("Prescription", saved.getId().toString(), hospitalId, null);
        return PrescriptionDTO.fromEntity(saved);
    }

    public PrescriptionDTO dispense(UUID hospitalId, UUID id) {
        Prescription rx = getRxWithTenantCheck(hospitalId, id);
        if (rx.getStatus() != PrescriptionStatus.PENDING)
            throw new IllegalArgumentException("Only PENDING prescriptions can be dispensed");

        String performedBy = getAuthenticatedEmail();

        // Validate stock for all items first
        for (PrescriptionItem item : rx.getItems()) {
            if (item.getMedicine().getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for " + item.getMedicine().getName()
                                + ": available=" + item.getMedicine().getStockQuantity()
                                + ", required=" + item.getQuantity());
            }
        }

        // Decrement stock and create audit trail
        for (PrescriptionItem item : rx.getItems()) {
            Medicine med = item.getMedicine();
            med.setStockQuantity(med.getStockQuantity() - item.getQuantity());
            med.setUpdatedAt(LocalDateTime.now());
            medicineRepository.save(med);

            StockTransaction txn = StockTransaction.builder()
                    .medicine(med).itemType("MEDICINE")
                    .hospital(rx.getHospital())
                    .transactionType(StockTransactionType.DISPENSED)
                    .quantityChange(-item.getQuantity())
                    .referenceId(rx.getId())
                    .performedBy(performedBy)
                    .notes("Dispensed for RX " + rx.getPrescriptionNumber())
                    .build();
            stockTransactionRepository.save(txn);
        }

        rx.setStatus(PrescriptionStatus.DISPENSED);
        rx.setDispensedAt(LocalDateTime.now());
        rx.setUpdatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(rx);
        auditService.logUpdate("Prescription", saved.getId().toString(), hospitalId, null);
        return PrescriptionDTO.fromEntity(saved);
    }

    public PrescriptionDTO cancel(UUID hospitalId, UUID id) {
        Prescription rx = getRxWithTenantCheck(hospitalId, id);
        if (rx.getStatus() != PrescriptionStatus.PENDING)
            throw new IllegalArgumentException("Only PENDING prescriptions can be cancelled");

        rx.setStatus(PrescriptionStatus.CANCELLED);
        rx.setUpdatedAt(LocalDateTime.now());

        Prescription saved = prescriptionRepository.save(rx);
        auditService.logUpdate("Prescription", saved.getId().toString(), hospitalId, null);
        return PrescriptionDTO.fromEntity(saved);
    }

    /**
     * Resolves a Doctor for prescription creation, enforcing identity checks:
     * - DOCTOR role: must prescribe under their own identity (doctorId in DTO is ignored)
     * - ADMIN/SUPER_ADMIN: may specify any doctor within the hospital
     */
    private Doctor resolveDoctor(UUID hospitalId, UUID requestedDoctorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_HOSPITAL_ADMIN"));

        if (isAdmin) {
            // Admins can create prescriptions on behalf of any doctor in the hospital
            if (requestedDoctorId == null)
                throw new IllegalArgumentException("Doctor ID is required");
            Doctor doctor = doctorRepository.findById(requestedDoctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", requestedDoctorId));
            if (!doctor.getHospital().getId().equals(hospitalId))
                throw new IllegalArgumentException("Doctor does not belong to this hospital");
            return doctor;
        }

        // For DOCTOR role: resolve from authenticated user, ignore client-supplied doctorId
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No doctor profile linked to authenticated user"));
        if (!doctor.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Doctor does not belong to this hospital");
        return doctor;
    }

    private String getAuthenticatedEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private Prescription getRxWithTenantCheck(UUID hospitalId, UUID id) {
        Prescription rx = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription", id));
        if (!rx.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Prescription does not belong to this hospital");
        return rx;
    }
}
