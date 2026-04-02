package com.healthcare.erp.service;

import com.healthcare.erp.dto.BedDTO;
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
public class BedService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<BedDTO> getByWard(UUID hospitalId, UUID wardId) {
        validateWardBelongsToHospital(hospitalId, wardId);
        auditService.logRead("Bed", "LIST:ward=" + wardId, hospitalId, null);
        return bedRepository.findByWardId(wardId).stream()
                .map(BedDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<BedDTO> getAvailable(UUID hospitalId) {
        auditService.logRead("Bed", "AVAILABLE", hospitalId, null);
        return bedRepository.findByHospitalIdAndStatus(hospitalId, BedStatus.AVAILABLE).stream()
                .map(BedDTO::fromEntity).toList();
    }

    public BedDTO create(UUID hospitalId, UUID wardId, BedDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", wardId));
        if (!ward.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Ward", wardId);

        if (bedRepository.existsByBedNumberAndWardId(dto.bedNumber(), wardId))
            throw new IllegalArgumentException("Bed '" + dto.bedNumber() + "' already exists in this ward");

        Bed bed = Bed.builder()
                .bedNumber(dto.bedNumber())
                .ward(ward)
                .hospital(hospital)
                .status(BedStatus.AVAILABLE)
                .build();

        Bed saved = bedRepository.save(bed);
        auditService.logCreate("Bed", saved.getId().toString(), hospitalId, null);
        return BedDTO.fromEntity(saved);
    }

    /**
     * Assign a patient to a bed (marks bed as OCCUPIED).
     */
    public BedDTO assignPatient(UUID hospitalId, UUID bedId, UUID patientId) {
        Bed bed = getBedWithTenantCheck(hospitalId, bedId);
        if (bed.getStatus() != BedStatus.AVAILABLE)
            throw new IllegalArgumentException("Bed is not available (current status: " + bed.getStatus() + ")");

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        if (!patient.getHospital().getId().equals(hospitalId))
            throw new IllegalArgumentException("Patient does not belong to this hospital");

        // P2 fix: prevent same patient from being assigned to multiple beds
        if (bedRepository.existsByPatientIdAndStatus(patientId, BedStatus.OCCUPIED))
            throw new IllegalArgumentException("Patient is already assigned to another bed");

        bed.setPatient(patient);
        bed.setStatus(BedStatus.OCCUPIED);
        bed.setAssignedAt(LocalDateTime.now());
        bed.setUpdatedAt(LocalDateTime.now());

        Bed saved = bedRepository.save(bed);
        auditService.log("ASSIGN", "Bed", bedId.toString(), hospitalId, null,
                "Patient " + patientId + " assigned to bed " + bed.getBedNumber());
        return BedDTO.fromEntity(saved);
    }

    /**
     * Release a patient from a bed (marks bed as AVAILABLE).
     */
    public BedDTO releasePatient(UUID hospitalId, UUID bedId) {
        Bed bed = getBedWithTenantCheck(hospitalId, bedId);
        if (bed.getStatus() != BedStatus.OCCUPIED)
            throw new IllegalArgumentException("Bed is not currently occupied");

        UUID patientId = bed.getPatient() != null ? bed.getPatient().getId() : null;
        bed.setPatient(null);
        bed.setStatus(BedStatus.AVAILABLE);
        bed.setAssignedAt(null);
        bed.setUpdatedAt(LocalDateTime.now());

        Bed saved = bedRepository.save(bed);
        auditService.log("RELEASE", "Bed", bedId.toString(), hospitalId, null,
                "Patient " + patientId + " released from bed " + bed.getBedNumber());
        return BedDTO.fromEntity(saved);
    }

    /**
     * Change bed status (e.g. to MAINTENANCE or RESERVED).
     */
    public BedDTO updateStatus(UUID hospitalId, UUID bedId, BedStatus newStatus) {
        Bed bed = getBedWithTenantCheck(hospitalId, bedId);
        if (bed.getStatus() == BedStatus.OCCUPIED && newStatus != BedStatus.AVAILABLE)
            throw new IllegalArgumentException("Release the patient before changing bed status");

        bed.setStatus(newStatus);
        bed.setUpdatedAt(LocalDateTime.now());

        Bed saved = bedRepository.save(bed);
        auditService.logUpdate("Bed", bedId.toString(), hospitalId, null);
        return BedDTO.fromEntity(saved);
    }

    private Bed getBedWithTenantCheck(UUID hospitalId, UUID bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ResourceNotFoundException("Bed", bedId));
        if (!bed.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Bed", bedId);
        return bed;
    }

    private void validateWardBelongsToHospital(UUID hospitalId, UUID wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", wardId));
        if (!ward.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Ward", wardId);
    }
}
