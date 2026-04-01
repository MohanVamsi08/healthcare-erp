package com.healthcare.erp.service;

import com.healthcare.erp.dto.StaffDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public List<StaffDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return staffRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                .stream().map(StaffDTO::fromEntity).toList();
    }

    public StaffDTO getById(UUID hospitalId, UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Staff", id);
        }
        return StaffDTO.fromEntity(staff);
    }

    public StaffDTO create(UUID hospitalId, StaffDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (staffRepository.existsByEmployeeIdAndHospitalId(dto.employeeId(), hospitalId)) {
            throw new IllegalArgumentException("Employee ID already exists in this hospital: " + dto.employeeId());
        }

        Staff staff = Staff.builder()
                .employeeId(dto.employeeId())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .role(dto.role())
                .hospital(hospital)
                .phone(dto.phone())
                .email(dto.email())
                .dateOfJoining(dto.dateOfJoining())
                .salary(dto.salary())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();

        if (dto.departmentId() != null) {
            Department dept = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.departmentId()));
            if (!dept.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("Department does not belong to this hospital");
            }
            staff.setDepartment(dept);
        }

        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.userId()));
            if (!user.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("User does not belong to this hospital");
            }
            staff.setUser(user);
        }

        Staff saved = staffRepository.save(staff);
        auditService.logCreate("Staff", saved.getId().toString(), hospitalId, null);
        return StaffDTO.fromEntity(saved);
    }

    public StaffDTO update(UUID hospitalId, UUID id, StaffDTO dto) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Staff", id);
        }

        staff.setFirstName(dto.firstName());
        staff.setLastName(dto.lastName());
        staff.setPhone(dto.phone());
        staff.setEmail(dto.email());
        staff.setSalary(dto.salary());
        if (dto.role() != null) staff.setRole(dto.role());
        if (dto.isActive() != null) staff.setActive(dto.isActive());

        Staff saved = staffRepository.save(staff);
        auditService.log("UPDATE", "Staff", id.toString(), hospitalId, null, null);
        return StaffDTO.fromEntity(saved);
    }

    public void deactivate(UUID hospitalId, UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        if (!staff.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Staff", id);
        }
        staff.setActive(false);
        staffRepository.save(staff);
        auditService.log("DEACTIVATE", "Staff", id.toString(), hospitalId, null, null);
    }
}
