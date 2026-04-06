package com.healthcare.erp.controller;

import com.healthcare.erp.dto.SupplierDTO;
import com.healthcare.erp.service.SupplierService;
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
@RequestMapping("/api/hospitals/{hospitalId}/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<SupplierDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(supplierService.getByHospital(hospitalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierDTO> create(@PathVariable UUID hospitalId,
                                               @RequestBody @Valid SupplierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(hospitalId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<SupplierDTO> update(@PathVariable UUID hospitalId, @PathVariable UUID id,
                                               @RequestBody @Valid SupplierDTO dto) {
        return ResponseEntity.ok(supplierService.update(hospitalId, id, dto));
    }
}
