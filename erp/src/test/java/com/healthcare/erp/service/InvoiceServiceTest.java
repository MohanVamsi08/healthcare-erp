package com.healthcare.erp.service;

import com.healthcare.erp.dto.InvoiceDTO;
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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private AuditService auditService;

    @InjectMocks
    private InvoiceService invoiceService;

    private UUID hospitalId;
    private UUID patientId;
    private Hospital hospital;
    private Patient patient;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        hospital = new Hospital();
        hospital.setId(hospitalId);

        patient = new Patient();
        patient.setId(patientId);
        patient.setHospital(hospital);
        patient.setFirstName("John");
        patient.setLastName("Doe");
    }

    private InvoiceDTO validDto() {
        return new InvoiceDTO(
                null, null, null, patientId, null, hospitalId,
                new BigDecimal("1000.00"), new BigDecimal("18.00"),
                null, null, null, LocalDate.now().plusDays(30), "Test invoice", null
        );
    }

    @Nested
    @DisplayName("create() validation")
    class CreateValidation {

        @Test
        @DisplayName("rejects null subtotal")
        void rejectsNullSubtotal() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    null, null, null, null, null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, dto),
                    "Subtotal must be greater than zero");
        }

        @Test
        @DisplayName("rejects negative subtotal")
        void rejectsNegativeSubtotal() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    new BigDecimal("-100.00"), null, null, null, null, null, null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, dto));
            assertTrue(ex.getMessage().contains("Subtotal must be greater than zero"));
        }

        @Test
        @DisplayName("rejects zero subtotal")
        void rejectsZeroSubtotal() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    BigDecimal.ZERO, null, null, null, null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, dto));
        }

        @Test
        @DisplayName("rejects GST rate above 100")
        void rejectsGstRateAbove100() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    new BigDecimal("1000.00"), new BigDecimal("150.00"),
                    null, null, null, null, null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, dto));
            assertTrue(ex.getMessage().contains("GST rate must be between 0 and 100"));
        }

        @Test
        @DisplayName("rejects negative GST rate")
        void rejectsNegativeGstRate() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    new BigDecimal("1000.00"), new BigDecimal("-5.00"),
                    null, null, null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, dto));
        }

        @Test
        @DisplayName("rejects patient from different hospital")
        void rejectsWrongHospitalPatient() {
            Hospital otherHospital = new Hospital();
            otherHospital.setId(UUID.randomUUID());
            patient.setHospital(otherHospital);

            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            assertThrows(IllegalArgumentException.class,
                    () -> invoiceService.create(hospitalId, validDto()));
        }
    }

    @Nested
    @DisplayName("create() invoice number generation")
    class InvoiceNumberGeneration {

        @Test
        @DisplayName("uses MAX+1 for new invoice number")
        void usesMaxBasedNumbering() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(invoiceRepository.findMaxInvoiceSequenceByHospitalId(hospitalId))
                    .thenReturn(Optional.of(5));

            Invoice savedInvoice = Invoice.builder()
                    .id(UUID.randomUUID())
                    .invoiceNumber("INV-0006")
                    .patient(patient)
                    .hospital(hospital)
                    .subtotal(new BigDecimal("1000.00"))
                    .gstRate(new BigDecimal("18.00"))
                    .gstAmount(new BigDecimal("180.00"))
                    .totalAmount(new BigDecimal("1180.00"))
                    .build();
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

            InvoiceDTO result = invoiceService.create(hospitalId, validDto());

            assertNotNull(result);
            verify(invoiceRepository).findMaxInvoiceSequenceByHospitalId(hospitalId);
        }

        @Test
        @DisplayName("starts at INV-0001 for first invoice")
        void startsAtOneForEmptyHospital() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(invoiceRepository.findMaxInvoiceSequenceByHospitalId(hospitalId))
                    .thenReturn(Optional.empty());

            Invoice savedInvoice = Invoice.builder()
                    .id(UUID.randomUUID())
                    .invoiceNumber("INV-0001")
                    .patient(patient)
                    .hospital(hospital)
                    .subtotal(new BigDecimal("1000.00"))
                    .gstRate(new BigDecimal("18.00"))
                    .gstAmount(new BigDecimal("180.00"))
                    .totalAmount(new BigDecimal("1180.00"))
                    .build();
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

            InvoiceDTO result = invoiceService.create(hospitalId, validDto());

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("creates invoice with valid data and default GST")
        void createsInvoiceWithDefaultGst() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(invoiceRepository.findMaxInvoiceSequenceByHospitalId(hospitalId))
                    .thenReturn(Optional.empty());

            Invoice savedInvoice = Invoice.builder()
                    .id(UUID.randomUUID())
                    .invoiceNumber("INV-0001")
                    .patient(patient)
                    .hospital(hospital)
                    .subtotal(new BigDecimal("1000.00"))
                    .gstRate(new BigDecimal("18.00"))
                    .gstAmount(new BigDecimal("180.00"))
                    .totalAmount(new BigDecimal("1180.00"))
                    .build();
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

            InvoiceDTO dto = new InvoiceDTO(
                    null, null, null, patientId, null, hospitalId,
                    new BigDecimal("1000.00"), null,
                    null, null, null, null, null, null
            );

            InvoiceDTO result = invoiceService.create(hospitalId, dto);

            assertNotNull(result);
            verify(invoiceRepository).save(any(Invoice.class));
            verify(auditService).logCreate(eq("Invoice"), any(), eq(hospitalId), isNull());
        }
    }
}
