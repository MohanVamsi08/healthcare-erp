package com.healthcare.erp.controller;

import jakarta.validation.Valid;
import com.healthcare.erp.dto.StaffDTO;
import com.healthcare.erp.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<StaffDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(staffService.getByHospitalId(hospitalId, pageable));
    }

    @GetMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<StaffDTO> getById(@PathVariable UUID hospitalId,
                                             @PathVariable UUID staffId) {
        return ResponseEntity.ok(staffService.getById(hospitalId, staffId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<StaffDTO> create(@PathVariable UUID hospitalId,
                                            @Valid @RequestBody StaffDTO dto) {
        return ResponseEntity.ok(staffService.create(hospitalId, dto));
    }

    @PutMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<StaffDTO> update(@PathVariable UUID hospitalId,
                                            @PathVariable UUID staffId,
                                            @Valid @RequestBody StaffDTO dto) {
        return ResponseEntity.ok(staffService.update(hospitalId, staffId, dto));
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> deactivate(@PathVariable UUID hospitalId,
                                            @PathVariable UUID staffId) {
        staffService.deactivate(hospitalId, staffId);
        return ResponseEntity.noContent().build();
    }
}
