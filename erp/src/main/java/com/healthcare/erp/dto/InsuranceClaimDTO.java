package com.healthcare.erp.dto;

import com.healthcare.erp.model.ClaimStatus;
import com.healthcare.erp.model.InsuranceClaim;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InsuranceClaimDTO(
        UUID id,
        String claimNumber,
        @NotNull(message = "Invoice ID is required")
        UUID invoiceId,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        String patientName,
        UUID hospitalId,
        @NotBlank(message = "Provider name is required")
        String providerName,
        @NotBlank(message = "Policy number is required")
        String policyNumber,
        @NotNull(message = "Claimed amount is required")
        @DecimalMin(value = "0.01", message = "Claimed amount must be greater than zero")
        BigDecimal claimedAmount,
        BigDecimal approvedAmount,
        ClaimStatus status,
        LocalDateTime submittedAt,
        LocalDateTime settledAt,
        String notes
) {
    public static InsuranceClaimDTO fromEntity(InsuranceClaim claim) {
        return new InsuranceClaimDTO(
                claim.getId(),
                claim.getClaimNumber(),
                claim.getInvoice().getId(),
                claim.getPatient().getId(),
                claim.getPatient().getFirstName() + " " + claim.getPatient().getLastName(),
                claim.getHospital().getId(),
                claim.getProviderName(),
                maskPolicyNumber(claim.getPolicyNumber()),
                claim.getClaimedAmount(),
                claim.getApprovedAmount(),
                claim.getStatus(),
                claim.getSubmittedAt(),
                claim.getSettledAt(),
                claim.getNotes()
        );
    }

    /**
     * Masks policy number in DTO output to prevent leakage.
     * Shows only last 4 characters: "****5678"
     */
    private static String maskPolicyNumber(String num) {
        if (num == null || num.length() <= 4) return num;
        return "****" + num.substring(num.length() - 4);
    }
}
