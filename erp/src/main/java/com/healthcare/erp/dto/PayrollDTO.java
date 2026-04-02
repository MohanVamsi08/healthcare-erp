package com.healthcare.erp.dto;

import com.healthcare.erp.model.Payroll;
import com.healthcare.erp.model.PayrollStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollDTO(
        UUID id,
        @NotNull UUID staffId,
        UUID hospitalId,
        @Min(1) @Max(12) int month,
        @Min(2000) int year,
        @NotNull @DecimalMin("0") BigDecimal baseSalary,
        BigDecimal allowances,
        BigDecimal deductions,
        BigDecimal netSalary,
        PayrollStatus status,
        LocalDateTime paidAt,
        String notes
) {
    public static PayrollDTO fromEntity(Payroll p) {
        return new PayrollDTO(
                p.getId(),
                p.getStaff().getId(),
                p.getHospital().getId(),
                p.getMonth(),
                p.getYear(),
                p.getBaseSalary(),
                p.getAllowances(),
                p.getDeductions(),
                p.getNetSalary(),
                p.getStatus(),
                p.getPaidAt(),
                p.getNotes()
        );
    }
}
