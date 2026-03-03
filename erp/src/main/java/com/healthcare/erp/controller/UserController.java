package com.healthcare.erp.controller;

import com.healthcare.erp.dto.CreateUserRequest;
import com.healthcare.erp.dto.UserDTO;
import com.healthcare.erp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * SUPER_ADMIN creates a HOSPITAL_ADMIN for a specific hospital.
     */
    @PostMapping("/api/admin/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserDTO> createHospitalAdmin(@Valid @RequestBody CreateUserRequest request) {
        UserDTO created = userService.createHospitalAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * HOSPITAL_ADMIN creates staff users for their hospital.
     */
    @PostMapping("/api/hospitals/{hospitalId}/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<UserDTO> createStaffUser(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody CreateUserRequest request) {
        UserDTO created = userService.createStaffUser(hospitalId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List all active users in a hospital.
     */
    @GetMapping("/api/hospitals/{hospitalId}/users")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(userService.getUsersByHospital(hospitalId));
    }

    /**
     * Get a specific user in a hospital.
     */
    @GetMapping("/api/hospitals/{hospitalId}/users/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<UserDTO> getUser(
            @PathVariable UUID hospitalId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUserById(hospitalId, userId));
    }

    /**
     * Update a staff user.
     */
    @PutMapping("/api/hospitals/{hospitalId}/users/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID hospitalId,
            @PathVariable UUID userId,
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(hospitalId, userId, request));
    }

    /**
     * Deactivate a staff user (soft delete).
     */
    @DeleteMapping("/api/hospitals/{hospitalId}/users/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable UUID hospitalId,
            @PathVariable UUID userId) {
        userService.deactivateUser(hospitalId, userId);
        return ResponseEntity.noContent().build();
    }
}
