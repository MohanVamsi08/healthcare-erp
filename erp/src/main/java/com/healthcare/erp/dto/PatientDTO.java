package com.healthcare.erp.dto;

import com.healthcare.erp.model.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientDTO(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String gender,
        String phone,
        String email,
        String aadhaarNumber,
        String bloodGroup,
        UUID hospitalId,
        boolean isActive,
        LocalDateTime createdAt) {
    public static PatientDTO fromEntity(Patient patient) {
        return new PatientDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAadhaarNumber(),
                patient.getBloodGroup(),
                patient.getHospital().getId(),
                patient.isActive(),
                patient.getCreatedAt());
    }
}
