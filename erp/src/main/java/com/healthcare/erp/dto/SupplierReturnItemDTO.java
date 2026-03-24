package com.healthcare.erp.dto;

import com.healthcare.erp.model.ReturnItemReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SupplierReturnItemDTO(
        UUID id,
        @NotNull(message = "Item type is required")
        String itemType,
        UUID medicineId,
        UUID supplyId,
        String itemName,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        @NotNull(message = "Return reason is required")
        ReturnItemReason reason
) {
}
