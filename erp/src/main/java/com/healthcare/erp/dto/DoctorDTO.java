package com.healthcare.erp.dto;

import com.healthcare.erp.model.Doctor;
import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorDTO(
        UUID id,
        String firstName,
        String lastName,
        String specialization,
        String licenseNumber,
        String phone,
        String email,
        UUID hospitalId,
        UUID departmentId,
        UUID userId,
        Boolean isActive,
        LocalDateTime createdAt) {

    public static DoctorDTO fromEntity(Doctor doctor) {
        return new DoctorDTO(
                doctor.getId(),
                doctor.getFirstName(),
                doctor.getLastName(),
                doctor.getSpecialization(),
                doctor.getLicenseNumber(),
                doctor.getPhone(),
                doctor.getEmail(),
                doctor.getHospital().getId(),
                doctor.getDepartment() != null ? doctor.getDepartment().getId() : null,
                doctor.getUser() != null ? doctor.getUser().getId() : null,
                doctor.isActive(),
                doctor.getCreatedAt());
    }
}
