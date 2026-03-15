package com.healthcare.erp.service;

import com.healthcare.erp.dto.StaffDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
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

    public List<StaffDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return staffRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                .stream().map(StaffDTO::fromEntity).toList();
    }

    public StaffDTO getById(UUID id) {
        return StaffDTO.fromEntity(staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id)));
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
            staff.setDepartment(dept);
        }

        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.userId()));
            staff.setUser(user);
        }

        return StaffDTO.fromEntity(staffRepository.save(staff));
    }

    public StaffDTO update(UUID id, StaffDTO dto) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));

        staff.setFirstName(dto.firstName());
        staff.setLastName(dto.lastName());
        staff.setPhone(dto.phone());
        staff.setEmail(dto.email());
        staff.setSalary(dto.salary());
        if (dto.role() != null) staff.setRole(dto.role());
        if (dto.isActive() != null) staff.setActive(dto.isActive());

        return StaffDTO.fromEntity(staffRepository.save(staff));
    }

    public void deactivate(UUID id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", id));
        staff.setActive(false);
        staffRepository.save(staff);
    }
}
