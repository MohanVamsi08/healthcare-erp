package com.healthcare.erp.service;

import com.healthcare.erp.dto.DoctorDTO;
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
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public DoctorDTO create(UUID hospitalId, DoctorDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        if (doctorRepository.existsByLicenseNumber(dto.licenseNumber())) {
            throw new IllegalArgumentException("Doctor with license number already exists: " + dto.licenseNumber());
        }

        Doctor doctor = Doctor.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .specialization(dto.specialization())
                .licenseNumber(dto.licenseNumber())
                .phone(dto.phone())
                .email(dto.email())
                .hospital(hospital)
                .build();

        if (dto.departmentId() != null) {
            Department dept = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.departmentId()));
            if (!dept.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("Department does not belong to this hospital");
            }
            doctor.setDepartment(dept);
        }

        if (dto.userId() != null) {
            User user = userRepository.findById(dto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.userId()));
            if (!user.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("User does not belong to this hospital");
            }
            doctor.setUser(user);
        }

        return DoctorDTO.fromEntity(doctorRepository.save(doctor));
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getByHospital(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return doctorRepository.findByHospitalIdAndIsActiveTrue(hospitalId)
                .stream().map(DoctorDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DoctorDTO getById(UUID hospitalId, UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }
        return DoctorDTO.fromEntity(doctor);
    }

    public DoctorDTO update(UUID hospitalId, UUID doctorId, DoctorDTO dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }

        doctor.setFirstName(dto.firstName());
        doctor.setLastName(dto.lastName());
        doctor.setSpecialization(dto.specialization());
        doctor.setPhone(dto.phone());
        doctor.setEmail(dto.email());

        if (dto.departmentId() != null) {
            Department dept = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", dto.departmentId()));
            if (!dept.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("Department does not belong to this hospital");
            }
            doctor.setDepartment(dept);
        }

        return DoctorDTO.fromEntity(doctorRepository.save(doctor));
    }

    public void deactivate(UUID hospitalId, UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }
        doctor.setActive(false);
        doctorRepository.save(doctor);
    }
}
