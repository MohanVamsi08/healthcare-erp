package com.healthcare.erp.service;

import com.healthcare.erp.dto.PaymentDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final HospitalRepository hospitalRepository;
    private final InvoiceService invoiceService;
    private final AuditService auditService;

    public List<PaymentDTO> getByInvoice(UUID hospitalId, UUID invoiceId) {
        // Validates invoice belongs to hospital
        invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId);
        auditService.logRead("Payment", "LIST:invoice=" + invoiceId, hospitalId, null);
        return paymentRepository.findByInvoiceId(invoiceId)
                .stream().map(PaymentDTO::fromEntity).toList();
    }

    public PaymentDTO recordPayment(UUID hospitalId, UUID invoiceId, PaymentDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Invoice invoice = invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId);

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot record payment on a CANCELLED invoice");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            throw new IllegalArgumentException("Invoice must be ISSUED before payments can be recorded");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalArgumentException("Invoice is already fully PAID");
        }

        // Validate payment amount
        if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // Prevent overpayment
        List<Payment> existingPayments = paymentRepository.findByInvoiceId(invoiceId);
        BigDecimal totalPaid = existingPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remaining = invoice.getTotalAmount().subtract(totalPaid);
        if (dto.amount().compareTo(remaining) > 0) {
            throw new IllegalArgumentException("Payment amount (" + dto.amount()
                    + ") exceeds remaining balance (" + remaining + ")");
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .hospital(hospital)
                .amount(dto.amount())
                .paymentMethod(dto.paymentMethod())
                .transactionReference(dto.transactionReference())
                .notes(dto.notes())
                .build();

        Payment saved = paymentRepository.save(payment);
        auditService.logCreate("Payment", saved.getId().toString(), hospitalId, null);

        // Auto-update invoice status based on total payments
        updateInvoicePaymentStatus(invoice);

        return PaymentDTO.fromEntity(saved);
    }

    private void updateInvoicePaymentStatus(Invoice invoice) {
        List<Payment> payments = paymentRepository.findByInvoiceId(invoice.getId());
        BigDecimal totalPaid = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            auditService.log("STATUS_CHANGE", "Invoice", invoice.getId().toString(),
                    invoice.getHospital().getId(), null, "-> PAID (full payment received)");
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            auditService.log("STATUS_CHANGE", "Invoice", invoice.getId().toString(),
                    invoice.getHospital().getId(), null,
                    "-> PARTIALLY_PAID (paid " + totalPaid + " of " + invoice.getTotalAmount() + ")");
        }
        invoice.setUpdatedAt(java.time.LocalDateTime.now());
        invoiceRepository.save(invoice);
    }
}
