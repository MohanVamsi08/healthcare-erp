package com.healthcare.erp.dto;

import com.healthcare.erp.model.Hospital;

import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalDTO(
        UUID id,
        String name,
        String gstin,
        String stateCode,
        String address,
        boolean isActive,
        LocalDateTime createdAt) {
    public static HospitalDTO fromEntity(Hospital hospital) {
        return new HospitalDTO(
                hospital.getId(),
                hospital.getName(),
                hospital.getGstin(),
                hospital.getStateCode(),
                hospital.getAddress(),
                hospital.isActive(),
                hospital.getCreatedAt());
    }

    public Hospital toEntity() {
        return Hospital.builder()
                .name(this.name)
                .gstin(this.gstin)
                .stateCode(this.stateCode)
                .address(this.address)
                .isActive(this.isActive)
                .build();
    }
}
