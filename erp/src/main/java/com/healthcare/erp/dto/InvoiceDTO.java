package com.healthcare.erp.dto;

import com.healthcare.erp.model.Invoice;
import com.healthcare.erp.model.InvoiceStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceDTO(
        UUID id,
        String invoiceNumber,
        UUID appointmentId,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        String patientName,
        UUID hospitalId,
        @NotNull(message = "Subtotal is required")
        @DecimalMin(value = "0.01", message = "Subtotal must be greater than zero")
        BigDecimal subtotal,
        @DecimalMin(value = "0.00", message = "GST rate cannot be negative")
        @DecimalMax(value = "100.00", message = "GST rate cannot exceed 100%")
        BigDecimal gstRate,
        BigDecimal gstAmount,
        BigDecimal totalAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        String notes,
        LocalDateTime createdAt
) {
    public static InvoiceDTO fromEntity(Invoice invoice) {
        return new InvoiceDTO(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getAppointment() != null ? invoice.getAppointment().getId() : null,
                invoice.getPatient().getId(),
                invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName(),
                invoice.getHospital().getId(),
                invoice.getSubtotal(),
                invoice.getGstRate(),
                invoice.getGstAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getNotes(),
                invoice.getCreatedAt()
        );
    }
}
