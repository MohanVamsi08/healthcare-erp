package com.healthcare.erp.controller;

import com.healthcare.erp.dto.DepartmentDTO;
import com.healthcare.erp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(departmentService.getByHospitalId(hospitalId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DepartmentDTO> createDepartment(
            @PathVariable UUID hospitalId,
            @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO created = departmentService.create(hospitalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable UUID hospitalId,
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.update(hospitalId, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        departmentService.delete(hospitalId, id);
        return ResponseEntity.noContent().build();
    }
}
