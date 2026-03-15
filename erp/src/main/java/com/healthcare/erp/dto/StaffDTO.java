package com.healthcare.erp.dto;

import com.healthcare.erp.model.Role;
import com.healthcare.erp.model.Staff;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StaffDTO(
        UUID id,
        String employeeId,
        String firstName,
        String lastName,
        Role role,
        UUID departmentId,
        UUID hospitalId,
        UUID userId,
        String phone,
        String email,
        LocalDate dateOfJoining,
        BigDecimal salary,
        Boolean isActive,
        LocalDateTime createdAt) {

    public static StaffDTO fromEntity(Staff staff) {
        return new StaffDTO(
                staff.getId(),
                staff.getEmployeeId(),
                staff.getFirstName(),
                staff.getLastName(),
                staff.getRole(),
                staff.getDepartment() != null ? staff.getDepartment().getId() : null,
                staff.getHospital().getId(),
                staff.getUser() != null ? staff.getUser().getId() : null,
                staff.getPhone(),
                staff.getEmail(),
                staff.getDateOfJoining(),
                staff.getSalary(),
                staff.isActive(),
                staff.getCreatedAt());
    }
}
