package com.healthcare.erp.dto;

import com.healthcare.erp.model.Supplier;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierDTO(
        UUID id,
        UUID hospitalId,
        @NotBlank(message = "Supplier name is required")
        String name,
        String contactPerson,
        String email,
        String phone,
        String address,
        String gstNumber,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static SupplierDTO fromEntity(Supplier s) {
        return new SupplierDTO(s.getId(), s.getHospital().getId(), s.getName(),
                s.getContactPerson(), s.getEmail(), s.getPhone(), s.getAddress(),
                s.getGstNumber(), s.isActive(), s.getCreatedAt());
    }
}
