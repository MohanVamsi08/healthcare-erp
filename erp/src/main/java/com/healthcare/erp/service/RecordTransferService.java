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
        // Fix #2: fromHospitalId must match the caller's hospital
        if (!dto.fromHospitalId().equals(hospitalId)) {
            throw new IllegalArgumentException("Transfer must originate from your own hospital");
        }
        if (dto.fromHospitalId().equals(dto.toHospitalId())) {
            throw new IllegalArgumentException("Source and destination hospitals must be different");
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

        // Fix #3: Strict status transition validation
        validateStatusTransition(r.getStatus(), newStatus);

        String previousStatus = r.getStatus().name();
        r.setStatus(newStatus);
        r.setUpdatedAt(LocalDateTime.now());
        RecordTransferRequest saved = transferRepository.save(r);
        auditService.log("STATUS_CHANGE", "RecordTransfer", id.toString(), hospitalId, null,
                previousStatus + " -> " + newStatus.name());
        return RecordTransferDTO.fromEntity(saved);
    }

    // Fix #5: No hard delete — use cancel instead for audit trail integrity
    public RecordTransferDTO cancel(UUID hospitalId, UUID id) {
        RecordTransferRequest r = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecordTransferRequest", id));
        if (!r.getFromHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Only the originating hospital can cancel a transfer");
        }
        if (r.getStatus() != TransferStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING transfers can be cancelled");
        }
        String previousStatus = r.getStatus().name();
        r.setStatus(TransferStatus.CANCELLED);
        r.setUpdatedAt(LocalDateTime.now());
        RecordTransferRequest saved = transferRepository.save(r);
        auditService.log("STATUS_CHANGE", "RecordTransfer", id.toString(), hospitalId, null,
                previousStatus + " -> CANCELLED");
        return RecordTransferDTO.fromEntity(saved);
    }

    private void validateStatusTransition(TransferStatus current, TransferStatus target) {
        boolean valid = switch (current) {
            case PENDING -> target == TransferStatus.APPROVED || target == TransferStatus.REJECTED;
            case APPROVED -> target == TransferStatus.COMPLETED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid status transition: " + current + " -> " + target);
        }
    }
}
