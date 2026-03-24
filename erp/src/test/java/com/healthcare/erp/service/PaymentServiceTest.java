package com.healthcare.erp.service;

import com.healthcare.erp.dto.PaymentDTO;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private AuditService auditService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID hospitalId;
    private UUID invoiceId;
    private Hospital hospital;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();

        hospital = new Hospital();
        hospital.setId(hospitalId);

        invoice = Invoice.builder()
                .id(invoiceId)
                .invoiceNumber("INV-0001")
                .hospital(hospital)
                .totalAmount(new BigDecimal("1180.00"))
                .status(InvoiceStatus.ISSUED)
                .build();
    }

    @Nested
    @DisplayName("recordPayment() validation")
    class RecordPaymentValidation {

        @Test
        @DisplayName("rejects null payment amount")
        void rejectsNullAmount() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    null, PaymentMethod.CARD,
                    null, "REF-001", null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
            assertTrue(ex.getMessage().contains("Payment amount must be greater than zero"));
        }

        @Test
        @DisplayName("rejects zero payment amount")
        void rejectsZeroAmount() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    BigDecimal.ZERO, PaymentMethod.CARD,
                    null, "REF-001", null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
        }

        @Test
        @DisplayName("rejects negative payment amount")
        void rejectsNegativeAmount() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    new BigDecimal("-50.00"), PaymentMethod.CARD,
                    null, "REF-001", null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
        }

        @Test
        @DisplayName("rejects overpayment exceeding remaining balance")
        void rejectsOverpayment() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            // Existing payment already covers 1000 of 1180
            Payment existing = Payment.builder()
                    .id(UUID.randomUUID())
                    .invoice(invoice)
                    .amount(new BigDecimal("1000.00"))
                    .build();
            when(paymentRepository.findByInvoiceId(invoiceId)).thenReturn(List.of(existing));

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    new BigDecimal("500.00"), PaymentMethod.CARD,
                    null, "REF-002", null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
            assertTrue(ex.getMessage().contains("exceeds remaining balance"));
        }

        @Test
        @DisplayName("rejects payment on CANCELLED invoice")
        void rejectsPaymentOnCancelledInvoice() {
            invoice.setStatus(InvoiceStatus.CANCELLED);
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    new BigDecimal("100.00"), PaymentMethod.CASH,
                    null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
        }

        @Test
        @DisplayName("rejects payment on DRAFT invoice")
        void rejectsPaymentOnDraftInvoice() {
            invoice.setStatus(InvoiceStatus.DRAFT);
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    new BigDecimal("100.00"), PaymentMethod.CASH,
                    null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> paymentService.recordPayment(hospitalId, invoiceId, dto));
        }
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("records valid payment on ISSUED invoice")
        void recordsValidPayment() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(paymentRepository.findByInvoiceId(invoiceId))
                    .thenReturn(Collections.emptyList()) // for overpayment check
                    .thenReturn(Collections.emptyList()); // for status update

            Payment savedPayment = Payment.builder()
                    .id(UUID.randomUUID())
                    .invoice(invoice)
                    .hospital(hospital)
                    .amount(new BigDecimal("500.00"))
                    .paymentMethod(PaymentMethod.CARD)
                    .transactionReference("REF-001")
                    .build();
            when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

            PaymentDTO dto = new PaymentDTO(
                    null, invoiceId, hospitalId,
                    new BigDecimal("500.00"), PaymentMethod.CARD,
                    null, "REF-001", null, null
            );

            PaymentDTO result = paymentService.recordPayment(hospitalId, invoiceId, dto);

            assertNotNull(result);
            verify(paymentRepository).save(any(Payment.class));
        }
    }
}
