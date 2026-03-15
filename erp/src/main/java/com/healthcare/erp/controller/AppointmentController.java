package com.healthcare.erp.controller;

import com.healthcare.erp.dto.AppointmentDTO;
import com.healthcare.erp.model.AppointmentStatus;
import com.healthcare.erp.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AppointmentDTO> create(
            @PathVariable UUID hospitalId,
            @RequestBody AppointmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(hospitalId, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<AppointmentDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(appointmentService.getByHospital(hospitalId));
    }

    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AppointmentDTO> getById(
            @PathVariable UUID hospitalId,
            @PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getById(hospitalId, appointmentId));
    }

    @PutMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AppointmentDTO> update(
            @PathVariable UUID hospitalId,
            @PathVariable UUID appointmentId,
            @RequestBody AppointmentDTO dto) {
        return ResponseEntity.ok(appointmentService.update(hospitalId, appointmentId, dto));
    }

    @PatchMapping("/{appointmentId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AppointmentDTO> updateStatus(
            @PathVariable UUID hospitalId,
            @PathVariable UUID appointmentId,
            @RequestBody Map<String, String> body) {
        AppointmentStatus status = AppointmentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(appointmentService.updateStatus(hospitalId, appointmentId, status));
    }
}
