package com.healthcare.erp.dto;

import java.util.UUID;

public record AuthResponse(
        String token,
        String email,
        String role,
        UUID hospitalId) {
}
