package com.healthcare.erp.dto;

import com.healthcare.erp.model.TransferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record RecordTransferDTO(
        UUID id,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        @NotNull(message = "Source hospital ID is required")
        UUID fromHospitalId,
        @NotNull(message = "Destination hospital ID is required")
        UUID toHospitalId,
        TransferStatus status,
        @NotBlank(message = "Transfer reason is required")
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
