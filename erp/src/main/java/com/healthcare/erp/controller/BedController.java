package com.healthcare.erp.controller;

import com.healthcare.erp.dto.BedDTO;
import com.healthcare.erp.model.BedStatus;
import com.healthcare.erp.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping("/wards/{wardId}/beds")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<BedDTO>> getByWard(@PathVariable UUID hospitalId,
                                                    @PathVariable UUID wardId) {
        return ResponseEntity.ok(bedService.getByWard(hospitalId, wardId));
    }

    @GetMapping("/beds/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<BedDTO>> getAvailable(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(bedService.getAvailable(hospitalId));
    }

    @PostMapping("/wards/{wardId}/beds")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<BedDTO> create(@PathVariable UUID hospitalId,
                                          @PathVariable UUID wardId,
                                          @Valid @RequestBody BedDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bedService.create(hospitalId, wardId, dto));
    }

    @PostMapping("/beds/{bedId}/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<BedDTO> assignPatient(@PathVariable UUID hospitalId,
                                                 @PathVariable UUID bedId,
                                                 @RequestBody Map<String, UUID> body) {
        UUID patientId = body.get("patientId");
        if (patientId == null) throw new IllegalArgumentException("patientId is required");
        return ResponseEntity.ok(bedService.assignPatient(hospitalId, bedId, patientId));
    }

    @PostMapping("/beds/{bedId}/release")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<BedDTO> releasePatient(@PathVariable UUID hospitalId,
                                                  @PathVariable UUID bedId) {
        return ResponseEntity.ok(bedService.releasePatient(hospitalId, bedId));
    }

    @PatchMapping("/beds/{bedId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<BedDTO> updateStatus(@PathVariable UUID hospitalId,
                                                @PathVariable UUID bedId,
                                                @RequestBody Map<String, String> body) {
        BedStatus status = BedStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(bedService.updateStatus(hospitalId, bedId, status));
    }
}
