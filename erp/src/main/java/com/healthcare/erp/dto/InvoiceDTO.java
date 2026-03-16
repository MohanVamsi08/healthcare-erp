package com.healthcare.erp.dto;

import com.healthcare.erp.model.Invoice;
import com.healthcare.erp.model.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceDTO(
        UUID id,
        String invoiceNumber,
        UUID appointmentId,
        UUID patientId,
        String patientName,
        UUID hospitalId,
        BigDecimal subtotal,
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
