package com.healthcare.erp.service;

import com.healthcare.erp.dto.PayrollDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PayrollDTO> getByPeriod(UUID hospitalId, int month, int year) {
        auditService.logRead("Payroll", "LIST:" + year + "-" + month, hospitalId, null);
        return payrollRepository.findByHospitalIdAndMonthAndYear(hospitalId, month, year).stream()
                .map(PayrollDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<PayrollDTO> getByStaff(UUID hospitalId, UUID staffId, int year) {
        // Tenant check: ensure staff belongs to this hospital
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
        if (!staff.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Staff", staffId);
        auditService.logRead("Payroll", "LIST:staff=" + staffId, hospitalId, null);
        return payrollRepository.findByStaffIdAndHospitalIdAndYear(staffId, hospitalId, year).stream()
                .map(PayrollDTO::fromEntity).toList();
    }

    public PayrollDTO create(UUID hospitalId, PayrollDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Staff staff = staffRepository.findById(dto.staffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff", dto.staffId()));
        if (!staff.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Staff does not belong to this hospital");

        if (payrollRepository.existsByStaffIdAndMonthAndYear(dto.staffId(), dto.month(), dto.year()))
            throw new IllegalArgumentException("Payroll already exists for this staff member for " +
                    dto.month() + "/" + dto.year());

        BigDecimal allowances = dto.allowances() != null ? dto.allowances() : BigDecimal.ZERO;
        BigDecimal deductions = dto.deductions() != null ? dto.deductions() : BigDecimal.ZERO;
        BigDecimal net = dto.baseSalary().add(allowances).subtract(deductions);

        Payroll payroll = Payroll.builder()
                .staff(staff)
                .hospital(hospital)
                .month(dto.month())
                .year(dto.year())
                .baseSalary(dto.baseSalary())
                .allowances(allowances)
                .deductions(deductions)
                .netSalary(net)
                .notes(dto.notes())
                .build();

        Payroll saved = payrollRepository.save(payroll);
        auditService.logCreate("Payroll", saved.getId().toString(), hospitalId, null);
        return PayrollDTO.fromEntity(saved);
    }

    /**
     * Process payroll (DRAFT → PROCESSED).
     */
    public PayrollDTO process(UUID hospitalId, UUID payrollId) {
        Payroll payroll = getWithTenantCheck(hospitalId, payrollId);
        if (payroll.getStatus() != PayrollStatus.DRAFT)
            throw new IllegalArgumentException("Only DRAFT payroll can be processed");

        payroll.setStatus(PayrollStatus.PROCESSED);
        payroll.setUpdatedAt(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);
        auditService.log("PROCESS", "Payroll", payrollId.toString(), hospitalId, null, null);
        return PayrollDTO.fromEntity(saved);
    }

    /**
     * Mark payroll as paid (PROCESSED → PAID).
     */
    public PayrollDTO markPaid(UUID hospitalId, UUID payrollId) {
        Payroll payroll = getWithTenantCheck(hospitalId, payrollId);
        if (payroll.getStatus() != PayrollStatus.PROCESSED)
            throw new IllegalArgumentException("Only PROCESSED payroll can be marked as paid");

        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());
        payroll.setUpdatedAt(LocalDateTime.now());

        Payroll saved = payrollRepository.save(payroll);
        auditService.log("PAY", "Payroll", payrollId.toString(), hospitalId, null, null);
        return PayrollDTO.fromEntity(saved);
    }

    private Payroll getWithTenantCheck(UUID hospitalId, UUID payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", payrollId));
        if (!payroll.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Payroll", payrollId);
        return payroll;
    }
}
