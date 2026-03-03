package com.healthcare.erp.controller;

import com.healthcare.erp.dto.DepartmentDTO;
import com.healthcare.erp.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/api/hospitals/{hospitalId}/departments")
    public ResponseEntity<List<DepartmentDTO>> getDepartmentsByHospital(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(departmentService.getByHospitalId(hospitalId));
    }

    @GetMapping("/api/departments/{id}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    @PostMapping("/api/hospitals/{hospitalId}/departments")
    public ResponseEntity<DepartmentDTO> createDepartment(
            @PathVariable UUID hospitalId,
            @RequestBody DepartmentDTO dto) {
        DepartmentDTO created = departmentService.create(hospitalId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/api/departments/{id}")
    public ResponseEntity<DepartmentDTO> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.update(id, dto));
    }

    @DeleteMapping("/api/departments/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
