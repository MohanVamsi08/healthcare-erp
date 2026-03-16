package com.healthcare.erp.controller;

import com.healthcare.erp.dto.HospitalDTO;
import com.healthcare.erp.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<HospitalDTO>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HospitalDTO> getHospitalById(@PathVariable UUID id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalDTO> createHospital(@RequestBody HospitalDTO dto) {
        HospitalDTO created = hospitalService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalDTO> updateHospital(@PathVariable UUID id, @RequestBody HospitalDTO dto) {
        return ResponseEntity.ok(hospitalService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteHospital(@PathVariable UUID id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
