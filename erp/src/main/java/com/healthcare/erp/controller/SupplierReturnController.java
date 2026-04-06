package com.healthcare.erp.controller;

import com.healthcare.erp.dto.SupplierReturnDTO;
import com.healthcare.erp.service.SupplierReturnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/supplier-returns")
@RequiredArgsConstructor
public class SupplierReturnController {

    private final SupplierReturnService returnService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<SupplierReturnDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(returnService.getByHospital(hospitalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierReturnDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(returnService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierReturnDTO> create(@PathVariable UUID hospitalId,
                                                     @RequestBody @Valid SupplierReturnDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.create(hospitalId, dto));
    }

    @PatchMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierReturnDTO> ship(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(returnService.markShipped(hospitalId, id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierReturnDTO> complete(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(returnService.complete(hospitalId, id));
    }
}
