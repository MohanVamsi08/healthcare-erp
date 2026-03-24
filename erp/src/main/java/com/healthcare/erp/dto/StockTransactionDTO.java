package com.healthcare.erp.dto;

import com.healthcare.erp.model.StockTransaction;
import com.healthcare.erp.model.StockTransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransactionDTO(
        UUID id,
        String itemType,
        UUID medicineId,
        String medicineName,
        UUID supplyId,
        String supplyName,
        UUID hospitalId,
        StockTransactionType transactionType,
        int quantityChange,
        UUID referenceId,
        String notes,
        LocalDateTime createdAt
) {
    public static StockTransactionDTO fromEntity(StockTransaction t) {
        return new StockTransactionDTO(t.getId(), t.getItemType(),
                t.getMedicine() != null ? t.getMedicine().getId() : null,
                t.getMedicine() != null ? t.getMedicine().getName() : null,
                t.getSupply() != null ? t.getSupply().getId() : null,
                t.getSupply() != null ? t.getSupply().getName() : null,
                t.getHospital().getId(), t.getTransactionType(),
                t.getQuantityChange(), t.getReferenceId(),
                t.getNotes(), t.getCreatedAt());
    }
}
