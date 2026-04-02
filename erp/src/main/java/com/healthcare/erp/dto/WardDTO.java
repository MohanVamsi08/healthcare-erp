package com.healthcare.erp.dto;

import com.healthcare.erp.model.Ward;
import com.healthcare.erp.model.WardType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record WardDTO(
        UUID id,
        @NotBlank String name,
        @NotNull WardType type,
        String floor,
        @Min(1) int totalBeds,
        UUID hospitalId,
        Boolean isActive
) {
    public static WardDTO fromEntity(Ward ward) {
        return new WardDTO(
                ward.getId(),
                ward.getName(),
                ward.getType(),
                ward.getFloor(),
                ward.getTotalBeds(),
                ward.getHospital().getId(),
                ward.isActive()
        );
    }
}
