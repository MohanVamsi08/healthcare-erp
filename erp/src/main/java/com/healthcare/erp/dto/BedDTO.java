package com.healthcare.erp.dto;

import com.healthcare.erp.model.Bed;
import com.healthcare.erp.model.BedStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record BedDTO(
        UUID id,
        @NotBlank String bedNumber,
        UUID wardId,
        UUID hospitalId,
        BedStatus status,
        UUID patientId,
        LocalDateTime assignedAt
) {
    public static BedDTO fromEntity(Bed bed) {
        return new BedDTO(
                bed.getId(),
                bed.getBedNumber(),
                bed.getWard().getId(),
                bed.getHospital().getId(),
                bed.getStatus(),
                bed.getPatient() != null ? bed.getPatient().getId() : null,
                bed.getAssignedAt()
        );
    }
}
