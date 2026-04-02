package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PayrollDTO;
import com.healthcare.erp.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<PayrollDTO>> getByPeriod(@PathVariable UUID hospitalId,
                                                         @RequestParam int month,
                                                         @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getByPeriod(hospitalId, month, year));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<PayrollDTO>> getByStaff(@PathVariable UUID hospitalId,
                                                        @PathVariable UUID staffId,
                                                        @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getByStaff(hospitalId, staffId, year));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PayrollDTO> create(@PathVariable UUID hospitalId,
                                              @Valid @RequestBody PayrollDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.create(hospitalId, dto));
    }

    @PostMapping("/{payrollId}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PayrollDTO> process(@PathVariable UUID hospitalId,
                                               @PathVariable UUID payrollId) {
        return ResponseEntity.ok(payrollService.process(hospitalId, payrollId));
    }

    @PostMapping("/{payrollId}/pay")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PayrollDTO> markPaid(@PathVariable UUID hospitalId,
                                                @PathVariable UUID payrollId) {
        return ResponseEntity.ok(payrollService.markPaid(hospitalId, payrollId));
    }
}
