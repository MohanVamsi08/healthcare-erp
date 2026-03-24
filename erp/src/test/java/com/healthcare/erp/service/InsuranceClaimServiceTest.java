package com.healthcare.erp.service;

import com.healthcare.erp.dto.InsuranceClaimDTO;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsuranceClaimServiceTest {

    @Mock private InsuranceClaimRepository claimRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private AuditService auditService;

    @InjectMocks
    private InsuranceClaimService claimService;

    private UUID hospitalId;
    private UUID patientId;
    private UUID invoiceId;
    private Hospital hospital;
    private Patient patient;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        invoiceId = UUID.randomUUID();

        hospital = new Hospital();
        hospital.setId(hospitalId);

        patient = new Patient();
        patient.setId(patientId);
        patient.setHospital(hospital);
        patient.setFirstName("Jane");
        patient.setLastName("Doe");

        invoice = Invoice.builder()
                .id(invoiceId)
                .hospital(hospital)
                .patient(patient)
                .subtotal(new BigDecimal("5000.00"))
                .totalAmount(new BigDecimal("5900.00"))
                .build();
    }

    private InsuranceClaimDTO validDto() {
        return new InsuranceClaimDTO(
                null, null, invoiceId, patientId, null, hospitalId,
                "Medicare", "POL-123456",
                new BigDecimal("5000.00"), null,
                null, null, null, "Test claim"
        );
    }

    @Nested
    @DisplayName("submit() validation")
    class SubmitValidation {

        @Test
        @DisplayName("rejects null claimed amount")
        void rejectsNullClaimedAmount() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InsuranceClaimDTO dto = new InsuranceClaimDTO(
                    null, null, invoiceId, patientId, null, hospitalId,
                    "Medicare", "POL-123456",
                    null, null, null, null, null, null
            );

            assertThrows(IllegalArgumentException.class,
                    () -> claimService.submit(hospitalId, dto));
        }

        @Test
        @DisplayName("rejects negative claimed amount")
        void rejectsNegativeClaimedAmount() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InsuranceClaimDTO dto = new InsuranceClaimDTO(
                    null, null, invoiceId, patientId, null, hospitalId,
                    "Medicare", "POL-123456",
                    new BigDecimal("-100.00"), null, null, null, null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> claimService.submit(hospitalId, dto));
            assertTrue(ex.getMessage().contains("Claimed amount must be greater than zero"));
        }

        @Test
        @DisplayName("rejects blank provider name")
        void rejectsBlankProviderName() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InsuranceClaimDTO dto = new InsuranceClaimDTO(
                    null, null, invoiceId, patientId, null, hospitalId,
                    "", "POL-123456",
                    new BigDecimal("5000.00"), null, null, null, null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> claimService.submit(hospitalId, dto));
            assertTrue(ex.getMessage().contains("Provider name is required"));
        }

        @Test
        @DisplayName("rejects blank policy number")
        void rejectsBlankPolicyNumber() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InsuranceClaimDTO dto = new InsuranceClaimDTO(
                    null, null, invoiceId, patientId, null, hospitalId,
                    "Medicare", "   ",
                    new BigDecimal("5000.00"), null, null, null, null, null
            );

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> claimService.submit(hospitalId, dto));
            assertTrue(ex.getMessage().contains("Policy number is required"));
        }

        @Test
        @DisplayName("happy path submit")
        void happyPathSubmit() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(invoiceService.getInvoiceWithTenantCheck(hospitalId, invoiceId)).thenReturn(invoice);
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

            InsuranceClaim savedClaim = InsuranceClaim.builder()
                    .id(UUID.randomUUID())
                    .claimNumber("CLM-ABCD1234")
                    .invoice(invoice)
                    .patient(patient)
                    .hospital(hospital)
                    .providerName("Medicare")
                    .policyNumber("POL-123456")
                    .claimedAmount(new BigDecimal("5000.00"))
                    .build();
            when(claimRepository.save(any(InsuranceClaim.class))).thenReturn(savedClaim);

            InsuranceClaimDTO result = claimService.submit(hospitalId, validDto());

            assertNotNull(result);
            verify(claimRepository).save(any(InsuranceClaim.class));
        }
    }

    @Nested
    @DisplayName("updateStatus() approval validation")
    class ApprovalValidation {

        private UUID claimId;
        private InsuranceClaim existingClaim;

        @BeforeEach
        void setUp() {
            claimId = UUID.randomUUID();
            existingClaim = InsuranceClaim.builder()
                    .id(claimId)
                    .claimNumber("CLM-12345678")
                    .invoice(invoice)
                    .patient(patient)
                    .hospital(hospital)
                    .providerName("Medicare")
                    .policyNumber("POL-123456")
                    .claimedAmount(new BigDecimal("5000.00"))
                    .status(ClaimStatus.UNDER_REVIEW)
                    .build();
        }

        @Test
        @DisplayName("rejects approval with null approved amount")
        void rejectsNullApprovedAmount() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));

            assertThrows(IllegalArgumentException.class,
                    () -> claimService.updateStatus(hospitalId, claimId, ClaimStatus.APPROVED, null));
        }

        @Test
        @DisplayName("rejects approval with negative approved amount")
        void rejectsNegativeApprovedAmount() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> claimService.updateStatus(hospitalId, claimId, ClaimStatus.APPROVED,
                            new BigDecimal("-100.00")));
            assertTrue(ex.getMessage().contains("Approved amount must be greater than zero"));
        }

        @Test
        @DisplayName("rejects approval with zero approved amount")
        void rejectsZeroApprovedAmount() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));

            assertThrows(IllegalArgumentException.class,
                    () -> claimService.updateStatus(hospitalId, claimId, ClaimStatus.APPROVED,
                            BigDecimal.ZERO));
        }

        @Test
        @DisplayName("rejects approved amount exceeding claimed amount")
        void rejectsAmountExceedingClaimed() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> claimService.updateStatus(hospitalId, claimId, ClaimStatus.APPROVED,
                            new BigDecimal("10000.00")));
            assertTrue(ex.getMessage().contains("cannot exceed claimed amount"));
        }

        @Test
        @DisplayName("approves with valid amount equal to claimed amount")
        void approvesWithValidAmount() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));
            when(claimRepository.save(any(InsuranceClaim.class))).thenReturn(existingClaim);

            InsuranceClaimDTO result = claimService.updateStatus(
                    hospitalId, claimId, ClaimStatus.APPROVED, new BigDecimal("5000.00"));

            assertNotNull(result);
            verify(claimRepository).save(any(InsuranceClaim.class));
        }

        @Test
        @DisplayName("approves with valid amount less than claimed amount")
        void approvesWithPartialAmount() {
            when(claimRepository.findById(claimId)).thenReturn(Optional.of(existingClaim));
            when(claimRepository.save(any(InsuranceClaim.class))).thenReturn(existingClaim);

            InsuranceClaimDTO result = claimService.updateStatus(
                    hospitalId, claimId, ClaimStatus.APPROVED, new BigDecimal("3000.00"));

            assertNotNull(result);
        }
    }
}
