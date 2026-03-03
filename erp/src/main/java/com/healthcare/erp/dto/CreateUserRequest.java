package com.healthcare.erp.dto;

import com.healthcare.erp.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role,
        UUID hospitalId) {
}
