package com.healthcare.erp.dto;

import com.healthcare.erp.model.Department;

import java.time.LocalDateTime;
import java.util.UUID;

public record DepartmentDTO(
        UUID id,
        String name,
        String code,
        UUID hospitalId,
        Boolean isActive,
        LocalDateTime createdAt) {
    public static DepartmentDTO fromEntity(Department department) {
        return new DepartmentDTO(
                department.getId(),
                department.getName(),
                department.getCode(),
                department.getHospital().getId(),
                department.isActive(),
                department.getCreatedAt());
    }
}
