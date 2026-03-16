package com.healthcare.erp.dto;

import com.healthcare.erp.model.Payment;
import com.healthcare.erp.model.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentDTO(
        UUID id,
        UUID invoiceId,
        UUID hospitalId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        LocalDateTime paymentDate,
        String transactionReference,
        String notes,
        LocalDateTime createdAt
) {
    public static PaymentDTO fromEntity(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getInvoice().getId(),
                payment.getHospital().getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                maskReference(payment.getTransactionReference()),
                payment.getNotes(),
                payment.getCreatedAt()
        );
    }

    /**
     * Masks transaction reference in DTO output to prevent leakage.
     * Shows only last 4 characters: "****5678"
     */
    private static String maskReference(String ref) {
        if (ref == null || ref.length() <= 4) return ref;
        return "****" + ref.substring(ref.length() - 4);
    }
}
