package com.healthcare.erp.dto;

import com.healthcare.erp.model.DosageForm;
import com.healthcare.erp.model.Medicine;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicineDTO(
        UUID id,
        UUID hospitalId,
        UUID supplierId,
        String supplierName,
        @NotBlank(message = "Medicine name is required")
        String name,
        String genericName,
        String manufacturer,
        String batchNumber,
        LocalDate expiryDate,
        @NotNull(message = "Dosage form is required")
        DosageForm dosageForm,
        String strength,
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
    public static MedicineDTO fromEntity(Medicine m) {
        return new MedicineDTO(m.getId(), m.getHospital().getId(),
                m.getSupplier() != null ? m.getSupplier().getId() : null,
                m.getSupplier() != null ? m.getSupplier().getName() : null,
                m.getName(), m.getGenericName(), m.getManufacturer(),
                m.getBatchNumber(), m.getExpiryDate(), m.getDosageForm(),
                m.getStrength(), m.getUnitPrice(), m.getStockQuantity(),
                m.getReorderLevel(), m.isActive(), m.getCreatedAt());
    }
}
