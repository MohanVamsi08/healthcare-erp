package com.healthcare.erp.dto;

import com.healthcare.erp.model.Role;
import jakarta.validation.constraints.*;

/**
 * DTO for updating an existing user. Password is NOT included here —
 * password resets should go through a dedicated endpoint.
 */
public record UpdateUserRequest(
        @NotBlank @Email String email,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role) {
}
