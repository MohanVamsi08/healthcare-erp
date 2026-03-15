package com.healthcare.erp.dto;

import com.healthcare.erp.model.Role;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Role role,
        UUID hospitalId) {
}

