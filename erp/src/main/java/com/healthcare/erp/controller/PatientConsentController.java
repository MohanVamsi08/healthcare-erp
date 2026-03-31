package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PatientConsentDTO;
import com.healthcare.erp.service.PatientConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/consents")
@RequiredArgsConstructor
public class PatientConsentController {

    private final PatientConsentService consentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<PatientConsentDTO>> getByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(consentService.getByHospital(hospitalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientConsentDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(consentService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientConsentDTO> create(@PathVariable UUID hospitalId, @RequestBody PatientConsentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consentService.create(hospitalId, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> delete(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        consentService.delete(hospitalId, id);
        return ResponseEntity.noContent().build();
    }
}
