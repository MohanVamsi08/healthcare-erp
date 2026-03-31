package com.healthcare.erp.dto;

import com.healthcare.erp.model.TransferStatus;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record RecordTransferDTO(
    UUID id,
    @NotNull
    UUID patientId,
    @NotNull
    UUID fromHospitalId,
    @NotNull
    UUID toHospitalId,
    TransferStatus status,
    @NotBlank
    String reason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RecordTransferDTO fromEntity(com.healthcare.erp.model.RecordTransferRequest r) {
        return new RecordTransferDTO(
                r.getId(),
                r.getPatient().getId(),
                r.getFromHospital().getId(),
                r.getToHospital().getId(),
                r.getStatus(),
                r.getReason(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
