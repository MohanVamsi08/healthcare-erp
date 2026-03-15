package com.healthcare.erp.dto;

import com.healthcare.erp.model.Role;
import com.healthcare.erp.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Role role,
        UUID hospitalId,
        Boolean isActive,
        LocalDateTime createdAt) {

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getHospital() != null ? user.getHospital().getId() : null,
                user.isActive(),
                user.getCreatedAt());
    }
}
