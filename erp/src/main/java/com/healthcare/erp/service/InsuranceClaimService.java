package com.healthcare.erp.service;

import com.healthcare.erp.dto.InsuranceClaimDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceClaimService {

    private final InsuranceClaimRepository claimRepository;
    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;
    private final InvoiceService invoiceService;
    private final AuditService auditService;

    public List<InsuranceClaimDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("InsuranceClaim", "LIST", hospitalId, null);
        return claimRepository.findByHospitalId(hospitalId)
                .stream().map(InsuranceClaimDTO::fromEntity).toList();
    }

    public InsuranceClaimDTO getById(UUID hospitalId, UUID claimId) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceClaim", claimId));
        if (!claim.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("InsuranceClaim", claimId);
        }
        auditService.logRead("InsuranceClaim", claimId.toString(), hospitalId, null);
        return InsuranceClaimDTO.fromEntity(claim);
    }

    public InsuranceClaimDTO submit(UUID hospitalId, InsuranceClaimDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Invoice invoice = invoiceService.getInvoiceWithTenantCheck(hospitalId, dto.invoiceId());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }
        if (!invoice.getPatient().getId().equals(dto.patientId())) {
            throw new IllegalArgumentException("Invoice does not belong to this patient");
        }

        // Generate claim number
        String claimNumber = "CLM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        InsuranceClaim claim = InsuranceClaim.builder()
                .claimNumber(claimNumber)
                .invoice(invoice)
                .patient(patient)
                .hospital(hospital)
                .providerName(dto.providerName())
                .policyNumber(dto.policyNumber())
                .claimedAmount(dto.claimedAmount())
                .notes(dto.notes())
                .build();

        InsuranceClaim saved = claimRepository.save(claim);
        auditService.logCreate("InsuranceClaim", saved.getId().toString(), hospitalId, null);
        return InsuranceClaimDTO.fromEntity(saved);
    }

    public InsuranceClaimDTO updateStatus(UUID hospitalId, UUID claimId, ClaimStatus newStatus,
                                           java.math.BigDecimal approvedAmount) {
        InsuranceClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("InsuranceClaim", claimId));
        if (!claim.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("InsuranceClaim", claimId);
        }

        // Validate status transitions
        validateStatusTransition(claim.getStatus(), newStatus);

        String previousStatus = claim.getStatus().name();
        claim.setStatus(newStatus);
        claim.setUpdatedAt(LocalDateTime.now());

        if (newStatus == ClaimStatus.APPROVED && approvedAmount != null) {
            claim.setApprovedAmount(approvedAmount);
        }
        if (newStatus == ClaimStatus.SETTLED) {
            claim.setSettledAt(LocalDateTime.now());
        }

        InsuranceClaim saved = claimRepository.save(claim);
        auditService.log("STATUS_CHANGE", "InsuranceClaim", claimId.toString(), hospitalId, null,
                previousStatus + " -> " + newStatus.name());
        return InsuranceClaimDTO.fromEntity(saved);
    }

    private void validateStatusTransition(ClaimStatus current, ClaimStatus target) {
        boolean valid = switch (current) {
            case SUBMITTED -> target == ClaimStatus.UNDER_REVIEW || target == ClaimStatus.REJECTED;
            case UNDER_REVIEW -> target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED;
            case APPROVED -> target == ClaimStatus.SETTLED;
            case REJECTED, SETTLED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition: " + current + " -> " + target);
        }
    }
}
