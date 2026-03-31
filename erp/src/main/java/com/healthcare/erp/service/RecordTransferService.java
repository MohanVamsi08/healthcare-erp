package com.healthcare.erp.service;

import com.healthcare.erp.dto.RecordTransferDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.RecordTransferRequest;
import com.healthcare.erp.model.TransferStatus;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.Patient;
import com.healthcare.erp.repository.RecordTransferRepository;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.PatientRepository;
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
public class RecordTransferService {

    private final RecordTransferRepository transferRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final PatientConsentRepository consentRepository;
    private final AuditService auditService;

    public List<RecordTransferDTO> getByFromHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("RecordTransfer", "LIST:from", hospitalId, null);
        return transferRepository.findByFromHospitalId(hospitalId)
                .stream().map(RecordTransferDTO::fromEntity).toList();
    }

    public List<RecordTransferDTO> getByToHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("RecordTransfer", "LIST:to", hospitalId, null);
        return transferRepository.findByToHospitalId(hospitalId)
                .stream().map(RecordTransferDTO::fromEntity).toList();
    }

    public RecordTransferDTO getById(UUID hospitalId, UUID id) {
        RecordTransferRequest r = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecordTransferRequest", id));
        // ensure either fromHospital or toHospital matches the path hospitalId
        if (!r.getFromHospital().getId().equals(hospitalId) && !r.getToHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("RecordTransferRequest", id);
        }
        auditService.logRead("RecordTransfer", id.toString(), hospitalId, null);
        return RecordTransferDTO.fromEntity(r);
    }

    public RecordTransferDTO create(UUID hospitalId, RecordTransferDTO dto) {
        // ensure the creator is the from-hospital (path hospitalId must match dto.fromHospitalId)
        if (!dto.fromHospitalId().equals(hospitalId)) {
            throw new IllegalArgumentException("fromHospitalId must match the path hospitalId");
        }

        Hospital from = hospitalRepository.findById(dto.fromHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", dto.fromHospitalId()));
        Hospital to = hospitalRepository.findById(dto.toHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", dto.toHospitalId()));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }

        // Ensure patient has given consent to the destination hospital
        var consentOpt = consentRepository.findByPatientIdAndHospitalId(patient.getId(), to.getId());
        if (consentOpt.isEmpty() || !consentOpt.get().isConsentGiven()) {
            throw new IllegalStateException("Patient has not given consent to share records with the destination hospital");
        }

        RecordTransferRequest r = RecordTransferRequest.builder()
                .patient(patient)
                .fromHospital(from)
                .toHospital(to)
                .status(TransferStatus.PENDING)
                .reason(dto.reason())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        RecordTransferRequest saved = transferRepository.save(r);
        auditService.logCreate("RecordTransfer", saved.getId().toString(), hospitalId, null);
        return RecordTransferDTO.fromEntity(saved);
    }

    public RecordTransferDTO updateStatus(UUID hospitalId, UUID id, TransferStatus newStatus) {
        RecordTransferRequest r = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecordTransferRequest", id));
        if (!r.getFromHospital().getId().equals(hospitalId) && !r.getToHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("RecordTransferRequest", id);
        }
        // enforce strict workflow: PENDING -> APPROVED/REJECTED (only toHospital), APPROVED -> COMPLETED (only toHospital)
        TransferStatus current = r.getStatus();
        if (current == TransferStatus.COMPLETED || current == TransferStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled transfer");
        }

        // Only destination hospital may approve or reject a pending transfer
        if (current == TransferStatus.PENDING) {
            if (newStatus != TransferStatus.APPROVED && newStatus != TransferStatus.REJECTED) {
                throw new IllegalStateException("PENDING transfers can only move to APPROVED or REJECTED");
            }
            if (!r.getToHospital().getId().equals(hospitalId) && r.getToHospital() != null) {
                throw new IllegalStateException("Only destination hospital can approve or reject this transfer");
            }
        }

        if (current == TransferStatus.APPROVED) {
            if (newStatus != TransferStatus.COMPLETED) {
                throw new IllegalStateException("APPROVED transfers can only move to COMPLETED");
            }
            if (!r.getToHospital().getId().equals(hospitalId)) {
                throw new IllegalStateException("Only destination hospital can complete this transfer");
            }
        }

        r.setStatus(newStatus);
        r.setUpdatedAt(LocalDateTime.now());
        RecordTransferRequest saved = transferRepository.save(r);
        auditService.logUpdate("RecordTransfer", id.toString(), hospitalId, "status=" + newStatus);
        return RecordTransferDTO.fromEntity(saved);
    }

    public RecordTransferDTO cancel(UUID hospitalId, UUID id) {
        RecordTransferRequest r = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecordTransferRequest", id));
        // Only fromHospital or SUPER_ADMIN (tenantGuard ensures roles) can cancel via path hospitalId
        if (!r.getFromHospital().getId().equals(hospitalId)) {
            throw new IllegalStateException("Only originating hospital can cancel the transfer");
        }
        r.setStatus(TransferStatus.CANCELLED);
        r.setUpdatedAt(LocalDateTime.now());
        RecordTransferRequest saved = transferRepository.save(r);
        auditService.logUpdate("RecordTransfer", id.toString(), hospitalId, "status=CANCELLED");
        return RecordTransferDTO.fromEntity(saved);
    }
}
