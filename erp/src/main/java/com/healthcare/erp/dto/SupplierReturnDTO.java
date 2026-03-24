package com.healthcare.erp.dto;

import com.healthcare.erp.model.SupplierReturn;
import com.healthcare.erp.model.SupplierReturnStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SupplierReturnDTO(
        UUID id,
        String returnNumber,
        UUID hospitalId,
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,
        String supplierName,
        SupplierReturnStatus status,
        @NotBlank(message = "Return reason is required")
        String reason,
        LocalDateTime initiatedAt,
        LocalDateTime completedAt,
        @NotEmpty(message = "At least one return item is required")
        @Valid
        List<SupplierReturnItemDTO> items
) {
    public static SupplierReturnDTO fromEntity(SupplierReturn r) {
        List<SupplierReturnItemDTO> itemDTOs = r.getItems().stream()
                .map(item -> new SupplierReturnItemDTO(
                        item.getId(), item.getItemType(),
                        item.getMedicine() != null ? item.getMedicine().getId() : null,
                        item.getSupply() != null ? item.getSupply().getId() : null,
                        item.getMedicine() != null ? item.getMedicine().getName()
                                : (item.getSupply() != null ? item.getSupply().getName() : null),
                        item.getQuantity(), item.getReason()))
                .toList();
        return new SupplierReturnDTO(r.getId(), r.getReturnNumber(),
                r.getHospital().getId(), r.getSupplier().getId(),
                r.getSupplier().getName(), r.getStatus(), r.getReason(),
                r.getInitiatedAt(), r.getCompletedAt(), itemDTOs);
    }
}
