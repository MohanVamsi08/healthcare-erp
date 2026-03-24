package com.healthcare.erp.dto;

import com.healthcare.erp.model.PurchaseOrder;
import com.healthcare.erp.model.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderDTO(
        UUID id,
        String orderNumber,
        UUID hospitalId,
        @NotNull(message = "Supplier ID is required")
        UUID supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        String notes,
        LocalDateTime orderedAt,
        String deliveredBy,
        String deliveryLocation,
        LocalDateTime deliveredAt,
        String receivedBy,
        LocalDateTime receivedAt,
        @NotEmpty(message = "At least one item is required")
        @Valid
        List<PurchaseOrderItemDTO> items
) {
    public static PurchaseOrderDTO fromEntity(PurchaseOrder po) {
        List<PurchaseOrderItemDTO> itemDTOs = po.getItems().stream()
                .map(item -> new PurchaseOrderItemDTO(
                        item.getId(), item.getItemType(),
                        item.getMedicine() != null ? item.getMedicine().getId() : null,
                        item.getSupply() != null ? item.getSupply().getId() : null,
                        item.getMedicine() != null ? item.getMedicine().getName()
                                : (item.getSupply() != null ? item.getSupply().getName() : null),
                        item.getQuantity(), item.getUnitCost()))
                .toList();
        return new PurchaseOrderDTO(po.getId(), po.getOrderNumber(),
                po.getHospital().getId(), po.getSupplier().getId(),
                po.getSupplier().getName(), po.getStatus(), po.getTotalAmount(),
                po.getNotes(), po.getOrderedAt(), po.getDeliveredBy(),
                po.getDeliveryLocation(), po.getDeliveredAt(),
                po.getReceivedBy(), po.getReceivedAt(), itemDTOs);
    }
}
