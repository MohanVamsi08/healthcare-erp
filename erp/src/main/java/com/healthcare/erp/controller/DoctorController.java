package com.healthcare.erp.controller;

import com.healthcare.erp.dto.DoctorDTO;
import com.healthcare.erp.service.DoctorService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DoctorDTO> create(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody DoctorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(hospitalId, dto));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<DoctorDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(doctorService.getByHospital(hospitalId));
    }

    @GetMapping("/{doctorId}")
    @PreAuthorize("isAuthenticated() and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DoctorDTO> getById(
            @PathVariable UUID hospitalId,
            @PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorService.getById(hospitalId, doctorId));
    }

    @PutMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DoctorDTO> update(
            @PathVariable UUID hospitalId,
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorDTO dto) {
        return ResponseEntity.ok(doctorService.update(hospitalId, doctorId, dto));
    }

    @DeleteMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID hospitalId,
            @PathVariable UUID doctorId) {
        doctorService.deactivate(hospitalId, doctorId);
        return ResponseEntity.noContent().build();
    }
}
