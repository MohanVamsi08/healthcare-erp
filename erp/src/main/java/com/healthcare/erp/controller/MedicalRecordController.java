package com.healthcare.erp.controller;

import com.healthcare.erp.dto.MedicalRecordDTO;
import com.healthcare.erp.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping("/api/hospitals/{hospitalId}/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalRecordDTO> create(
            @PathVariable UUID hospitalId,
            @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicalRecordService.create(hospitalId, dto));
    }

    @GetMapping("/api/hospitals/{hospitalId}/patients/{patientId}/medical-records")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<List<MedicalRecordDTO>> getByPatient(
            @PathVariable UUID hospitalId,
            @PathVariable UUID patientId) {
        return ResponseEntity.ok(medicalRecordService.getByPatient(hospitalId, patientId));
    }

    @GetMapping("/api/hospitals/{hospitalId}/medical-records/{recordId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR', 'NURSE')")
    public ResponseEntity<MedicalRecordDTO> getById(
            @PathVariable UUID hospitalId,
            @PathVariable UUID recordId) {
        return ResponseEntity.ok(medicalRecordService.getById(hospitalId, recordId));
    }

    @PutMapping("/api/hospitals/{hospitalId}/medical-records/{recordId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DOCTOR')")
    public ResponseEntity<MedicalRecordDTO> update(
            @PathVariable UUID hospitalId,
            @PathVariable UUID recordId,
            @RequestBody MedicalRecordDTO dto) {
        return ResponseEntity.ok(medicalRecordService.update(hospitalId, recordId, dto));
    }
}
