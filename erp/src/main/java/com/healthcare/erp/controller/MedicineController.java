package com.healthcare.erp.controller;

import com.healthcare.erp.dto.MedicineDTO;
import com.healthcare.erp.service.MedicineService;
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
@RequestMapping("/api/hospitals/{hospitalId}/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<MedicineDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(medicineService.getByHospital(hospitalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<MedicineDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(medicineService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<MedicineDTO> create(@PathVariable UUID hospitalId,
                                               @RequestBody @Valid MedicineDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicineService.create(hospitalId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<MedicineDTO> update(@PathVariable UUID hospitalId, @PathVariable UUID id,
                                               @RequestBody @Valid MedicineDTO dto) {
        return ResponseEntity.ok(medicineService.update(hospitalId, id, dto));
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<MedicineDTO> addStock(@PathVariable UUID hospitalId, @PathVariable UUID id,
                                                 @RequestBody Map<String, Object> body) {
        int quantity = (int) body.get("quantity");
        String notes = (String) body.getOrDefault("notes", null);
        return ResponseEntity.ok(medicineService.addStock(hospitalId, id, quantity, notes));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<MedicineDTO>> getLowStock(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(medicineService.getLowStock(hospitalId));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<MedicineDTO>> getExpiring(@PathVariable UUID hospitalId,
                                                          @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(medicineService.getExpiring(hospitalId, days));
    }
}
