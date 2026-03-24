package com.healthcare.erp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PrescriptionItemDTO(
        UUID id,
        @NotNull(message = "Medicine ID is required")
        UUID medicineId,
        String medicineName,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        String dosageInstructions
) {
}
