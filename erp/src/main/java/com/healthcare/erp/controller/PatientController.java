package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PatientDTO;
import com.healthcare.erp.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/api/hospitals/{hospitalId}/patients")
    public ResponseEntity<List<PatientDTO>> getPatientsByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(patientService.getByHospitalId(hospitalId));
    }

    @GetMapping("/api/patients/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getById(id));
    }

    @PostMapping("/api/hospitals/{hospitalId}/patients")
    public ResponseEntity<PatientDTO> createPatient(
            @PathVariable UUID hospitalId,
            @RequestBody PatientDTO dto) {
        PatientDTO created = patientService.create(hospitalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/patients/{id}")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable UUID id,
            @RequestBody PatientDTO dto) {
        return ResponseEntity.ok(patientService.update(id, dto));
    }

    @DeleteMapping("/api/patients/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
