package com.healthcare.erp.service;

import com.healthcare.erp.dto.DepartmentDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Department;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.repository.DepartmentRepository;
import com.healthcare.erp.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;

    public List<DepartmentDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return departmentRepository.findByHospitalId(hospitalId)
                .stream()
                .map(DepartmentDTO::fromEntity)
                .toList();
    }

    public DepartmentDTO getById(UUID hospitalId, UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (!department.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Department", id);
        }
        return DepartmentDTO.fromEntity(department);
    }

    public DepartmentDTO create(UUID hospitalId, DepartmentDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Department department = Department.builder()
                .name(dto.name())
                .code(dto.code())
                .hospital(hospital)
                .isActive(dto.isActive())
                .build();

        Department saved = departmentRepository.save(department);
        return DepartmentDTO.fromEntity(saved);
    }

    public DepartmentDTO update(UUID hospitalId, UUID id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (!department.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Department", id);
        }

        department.setName(dto.name());
        department.setCode(dto.code());
        department.setActive(dto.isActive());

        Department updated = departmentRepository.save(department);
        return DepartmentDTO.fromEntity(updated);
    }

    public void delete(UUID hospitalId, UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        if (!department.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Department", id);
        }
        departmentRepository.deleteById(id);
    }
}
