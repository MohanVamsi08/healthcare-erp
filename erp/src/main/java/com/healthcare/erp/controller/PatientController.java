package com.healthcare.erp.controller;

import jakarta.validation.Valid;
import com.healthcare.erp.dto.PatientDTO;
import com.healthcare.erp.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<PatientDTO>> getPatientsByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(patientService.getByHospitalId(hospitalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientDTO> createPatient(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody PatientDTO dto) {
        PatientDTO created = patientService.create(hospitalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable UUID hospitalId,
            @PathVariable UUID id,
            @Valid @RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.update(hospitalId, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        patientService.delete(hospitalId, id);
        return ResponseEntity.noContent().build();
    }
}
