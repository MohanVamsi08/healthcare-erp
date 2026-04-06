package com.healthcare.erp.service;

import com.healthcare.erp.dto.WardDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.Ward;
import com.healthcare.erp.repository.BedRepository;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.WardRepository;
import com.healthcare.erp.security.AuditService;
import com.healthcare.erp.model.BedStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WardService {

    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<WardDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("Ward", "LIST", hospitalId, null);
        return wardRepository.findByHospitalId(hospitalId).stream()
                .map(WardDTO::fromEntity).toList();
    }
    public Page<WardDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("Ward", "LIST", hospitalId, null);
        return wardRepository.findByHospitalId(hospitalId, pageable)
                .map(WardDTO::fromEntity);
    }


    @Transactional(readOnly = true)
    public WardDTO getById(UUID hospitalId, UUID wardId) {
        Ward ward = getWardWithTenantCheck(hospitalId, wardId);
        auditService.logRead("Ward", wardId.toString(), hospitalId, null);
        return WardDTO.fromEntity(ward);
    }

    public WardDTO create(UUID hospitalId, WardDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (wardRepository.existsByNameAndHospitalId(dto.name(), hospitalId))
            throw new IllegalArgumentException("Ward '" + dto.name() + "' already exists in this hospital");

        Ward ward = Ward.builder()
                .name(dto.name())
                .type(dto.type())
                .floor(dto.floor())
                .totalBeds(dto.totalBeds())
                .hospital(hospital)
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();

        Ward saved = wardRepository.save(ward);
        auditService.logCreate("Ward", saved.getId().toString(), hospitalId, null);
        return WardDTO.fromEntity(saved);
    }

    public WardDTO update(UUID hospitalId, UUID wardId, WardDTO dto) {
        Ward ward = getWardWithTenantCheck(hospitalId, wardId);

        ward.setName(dto.name());
        ward.setType(dto.type());
        ward.setFloor(dto.floor());
        ward.setTotalBeds(dto.totalBeds());
        if (dto.isActive() != null) ward.setActive(dto.isActive());
        ward.setUpdatedAt(LocalDateTime.now());

        Ward saved = wardRepository.save(ward);
        auditService.logUpdate("Ward", wardId.toString(), hospitalId, null);
        return WardDTO.fromEntity(saved);
    }

    public void delete(UUID hospitalId, UUID wardId) {
        Ward ward = getWardWithTenantCheck(hospitalId, wardId);
        long occupied = bedRepository.countByWardIdAndStatus(wardId, BedStatus.OCCUPIED);
        if (occupied > 0)
            throw new IllegalArgumentException("Cannot delete ward with " + occupied + " occupied beds");
        wardRepository.delete(ward);
        auditService.logDelete("Ward", wardId.toString(), hospitalId, null);
    }

    private Ward getWardWithTenantCheck(UUID hospitalId, UUID wardId) {
        Ward ward = wardRepository.findById(wardId)
                .orElseThrow(() -> new ResourceNotFoundException("Ward", wardId));
        if (!ward.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Ward", wardId);
        return ward;
    }
}
