package com.healthcare.erp.dto;

import com.healthcare.erp.model.HospitalSupply;
import com.healthcare.erp.model.SupplyCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HospitalSupplyDTO(
        UUID id,
        UUID hospitalId,
        UUID supplierId,
        String supplierName,
        @NotBlank(message = "Supply name is required")
        String name,
        @NotNull(message = "Category is required")
        SupplyCategory category,
        String manufacturer,
        String batchNumber,
        LocalDate expiryDate,
        String unit,
        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.01", message = "Unit price must be greater than zero")
        BigDecimal unitPrice,
        @Min(value = 0, message = "Stock quantity cannot be negative")
        int stockQuantity,
        @Min(value = 0, message = "Reorder level cannot be negative")
        int reorderLevel,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static HospitalSupplyDTO fromEntity(HospitalSupply s) {
        return new HospitalSupplyDTO(s.getId(), s.getHospital().getId(),
                s.getSupplier() != null ? s.getSupplier().getId() : null,
                s.getSupplier() != null ? s.getSupplier().getName() : null,
                s.getName(), s.getCategory(), s.getManufacturer(),
                s.getBatchNumber(), s.getExpiryDate(), s.getUnit(),
                s.getUnitPrice(), s.getStockQuantity(), s.getReorderLevel(),
                s.isActive(), s.getCreatedAt());
    }
}
