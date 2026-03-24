package com.healthcare.erp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemDTO(
        UUID id,
        @NotNull(message = "Item type is required (MEDICINE or SUPPLY)")
        String itemType,
        UUID medicineId,
        UUID supplyId,
        String itemName,
        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,
        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.01", message = "Unit cost must be greater than zero")
        BigDecimal unitCost
) {
}
