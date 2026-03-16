package com.healthcare.erp.dto;

import com.healthcare.erp.model.ClaimStatus;
import com.healthcare.erp.model.InsuranceClaim;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InsuranceClaimDTO(
        UUID id,
        String claimNumber,
        UUID invoiceId,
        UUID patientId,
        String patientName,
        UUID hospitalId,
        String providerName,
        String policyNumber,
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
