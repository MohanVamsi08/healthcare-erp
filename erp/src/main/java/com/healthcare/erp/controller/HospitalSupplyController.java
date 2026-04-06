package com.healthcare.erp.controller;

import com.healthcare.erp.dto.HospitalSupplyDTO;
import com.healthcare.erp.service.HospitalSupplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/supplies")
@RequiredArgsConstructor
public class HospitalSupplyController {

    private final HospitalSupplyService supplyService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<HospitalSupplyDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(supplyService.getByHospital(hospitalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<HospitalSupplyDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(supplyService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<HospitalSupplyDTO> create(@PathVariable UUID hospitalId,
                                                     @RequestBody @Valid HospitalSupplyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplyService.create(hospitalId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<HospitalSupplyDTO> update(@PathVariable UUID hospitalId, @PathVariable UUID id,
                                                     @RequestBody @Valid HospitalSupplyDTO dto) {
        return ResponseEntity.ok(supplyService.update(hospitalId, id, dto));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<HospitalSupplyDTO> addStock(@PathVariable UUID hospitalId, @PathVariable UUID id,
                                                       @RequestBody Map<String, Object> body) {
        int quantity = (int) body.get("quantity");
        String notes = (String) body.getOrDefault("notes", null);
        return ResponseEntity.ok(supplyService.addStock(hospitalId, id, quantity, notes));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<HospitalSupplyDTO>> getLowStock(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(supplyService.getLowStock(hospitalId));
    }
}
