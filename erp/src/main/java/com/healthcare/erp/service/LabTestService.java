package com.healthcare.erp.service;

import com.healthcare.erp.dto.LabTestDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.LabTest;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.LabTestRepository;
import com.healthcare.erp.security.AuditService;
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
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final HospitalRepository hospitalRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<LabTestDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("LabTest", "LIST", hospitalId, null);
        return labTestRepository.findByHospitalId(hospitalId).stream()
                .map(LabTestDTO::fromEntity).toList();
    }
    public Page<LabTestDTO> getByHospital(UUID hospitalId, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("LabTest", "LIST", hospitalId, null);
        return labTestRepository.findByHospitalId(hospitalId, pageable)
                .map(LabTestDTO::fromEntity);
    }


    @Transactional(readOnly = true)
    public LabTestDTO getById(UUID hospitalId, UUID testId) {
        LabTest test = getWithTenantCheck(hospitalId, testId);
        auditService.logRead("LabTest", testId.toString(), hospitalId, null);
        return LabTestDTO.fromEntity(test);
    }

    public LabTestDTO create(UUID hospitalId, LabTestDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (labTestRepository.existsByTestCodeAndHospitalId(dto.testCode(), hospitalId))
            throw new IllegalArgumentException("Test code '" + dto.testCode() + "' already exists");

        LabTest test = LabTest.builder()
                .testName(dto.testName())
                .testCode(dto.testCode())
                .category(dto.category())
                .cost(dto.cost())
                .hospital(hospital)
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();

        LabTest saved = labTestRepository.save(test);
        auditService.logCreate("LabTest", saved.getId().toString(), hospitalId, null);
        return LabTestDTO.fromEntity(saved);
    }

    public LabTestDTO update(UUID hospitalId, UUID testId, LabTestDTO dto) {
        LabTest test = getWithTenantCheck(hospitalId, testId);

        test.setTestName(dto.testName());
        test.setTestCode(dto.testCode());
        test.setCategory(dto.category());
        test.setCost(dto.cost());
        if (dto.isActive() != null) test.setActive(dto.isActive());
        test.setUpdatedAt(LocalDateTime.now());

        LabTest saved = labTestRepository.save(test);
        auditService.logUpdate("LabTest", testId.toString(), hospitalId, null);
        return LabTestDTO.fromEntity(saved);
    }

    private LabTest getWithTenantCheck(UUID hospitalId, UUID testId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", testId));
        if (!test.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("LabTest", testId);
        return test;
    }
}
