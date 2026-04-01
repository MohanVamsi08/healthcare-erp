package com.healthcare.erp.controller;

import com.healthcare.erp.dto.RecordTransferDTO;
import com.healthcare.erp.model.TransferStatus;
import com.healthcare.erp.service.RecordTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/transfers")
@RequiredArgsConstructor
public class RecordTransferController {

    private final RecordTransferService transferService;

    @GetMapping("/from")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<RecordTransferDTO>> getByFrom(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(transferService.getByFromHospital(hospitalId));
    }

    @GetMapping("/to")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<RecordTransferDTO>> getByTo(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(transferService.getByToHospital(hospitalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<RecordTransferDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(transferService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<RecordTransferDTO> create(@PathVariable UUID hospitalId, @Valid @RequestBody RecordTransferDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferService.create(hospitalId, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<RecordTransferDTO> updateStatus(@PathVariable UUID hospitalId, @PathVariable UUID id, @RequestParam TransferStatus status) {
        return ResponseEntity.ok(transferService.updateStatus(hospitalId, id, status));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<RecordTransferDTO> cancel(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(transferService.cancel(hospitalId, id));
    }
}
