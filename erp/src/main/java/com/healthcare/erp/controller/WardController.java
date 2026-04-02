package com.healthcare.erp.controller;

import com.healthcare.erp.dto.WardDTO;
import com.healthcare.erp.service.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<WardDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(wardService.getByHospital(hospitalId));
    }

    @GetMapping("/{wardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<WardDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID wardId) {
        return ResponseEntity.ok(wardService.getById(hospitalId, wardId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<WardDTO> create(@PathVariable UUID hospitalId,
                                           @Valid @RequestBody WardDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wardService.create(hospitalId, dto));
    }

    @PutMapping("/{wardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<WardDTO> update(@PathVariable UUID hospitalId,
                                           @PathVariable UUID wardId,
                                           @Valid @RequestBody WardDTO dto) {
        return ResponseEntity.ok(wardService.update(hospitalId, wardId, dto));
    }

    @DeleteMapping("/{wardId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> delete(@PathVariable UUID hospitalId, @PathVariable UUID wardId) {
        wardService.delete(hospitalId, wardId);
        return ResponseEntity.noContent().build();
    }
}
