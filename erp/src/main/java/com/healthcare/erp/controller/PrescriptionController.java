package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PrescriptionDTO;
import com.healthcare.erp.service.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<PrescriptionDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(prescriptionService.getByHospital(hospitalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PrescriptionDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PrescriptionDTO> create(@PathVariable UUID hospitalId,
                                                   @RequestBody @Valid PrescriptionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prescriptionService.create(hospitalId, dto));
    }

    @PatchMapping("/{id}/dispense")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PrescriptionDTO> dispense(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.dispense(hospitalId, id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PrescriptionDTO> cancel(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(prescriptionService.cancel(hospitalId, id));
    }
}
