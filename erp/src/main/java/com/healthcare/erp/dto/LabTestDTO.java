package com.healthcare.erp.dto;

import com.healthcare.erp.model.LabTest;
import com.healthcare.erp.model.LabTestCategory;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record LabTestDTO(
        UUID id,
        @NotBlank String testName,
        @NotBlank String testCode,
        @NotNull LabTestCategory category,
        @NotNull @DecimalMin("0.01") BigDecimal cost,
        UUID hospitalId,
        Boolean isActive
) {
    public static LabTestDTO fromEntity(LabTest test) {
        return new LabTestDTO(
                test.getId(),
                test.getTestName(),
                test.getTestCode(),
                test.getCategory(),
                test.getCost(),
                test.getHospital().getId(),
                test.isActive()
        );
    }
}
