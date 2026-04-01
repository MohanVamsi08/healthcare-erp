package com.healthcare.erp.service;

import com.healthcare.erp.dto.HospitalDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    public List<HospitalDTO> getAllHospitals() {
        return hospitalRepository.findAll()
                .stream()
                .map(HospitalDTO::fromEntity)
                .toList();
    }

    public HospitalDTO getById(UUID id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", id));
        auditService.logRead("Hospital", id.toString(), id, null);
        return HospitalDTO.fromEntity(hospital);
    }

    public HospitalDTO create(HospitalDTO dto) {
        Hospital hospital = dto.toEntity();
        Hospital saved = hospitalRepository.save(hospital);
        auditService.logCreate("Hospital", saved.getId().toString(), saved.getId(), null);
        return HospitalDTO.fromEntity(saved);
    }

    public HospitalDTO update(UUID id, HospitalDTO dto) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", id));

        hospital.setName(dto.name());
        hospital.setGstin(dto.gstin());
        hospital.setStateCode(dto.stateCode());
        hospital.setAddress(dto.address());
        hospital.setActive(dto.isActive());

        Hospital updated = hospitalRepository.save(hospital);
        auditService.log("UPDATE", "Hospital", id.toString(), id, null, null);
        return HospitalDTO.fromEntity(updated);
    }

    public void delete(UUID id) {
        if (!hospitalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hospital", id);
        }
        hospitalRepository.deleteById(id);
        auditService.logDelete("Hospital", id.toString(), id, null);
    }
}
