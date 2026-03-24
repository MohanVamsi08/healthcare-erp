package com.healthcare.erp.controller;

import com.healthcare.erp.dto.InvoiceDTO;
import com.healthcare.erp.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<InvoiceDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(invoiceService.getByHospital(hospitalId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<InvoiceDTO>> getByPatient(@PathVariable UUID hospitalId,
                                                          @PathVariable UUID patientId) {
        return ResponseEntity.ok(invoiceService.getByPatient(hospitalId, patientId));
    }

    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InvoiceDTO> getById(@PathVariable UUID hospitalId,
                                               @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.getById(hospitalId, invoiceId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InvoiceDTO> create(@PathVariable UUID hospitalId,
                                              @RequestBody @Valid InvoiceDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(hospitalId, dto));
    }

    @PatchMapping("/{invoiceId}/issue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InvoiceDTO> issue(@PathVariable UUID hospitalId,
                                             @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.issue(hospitalId, invoiceId));
    }

    @PatchMapping("/{invoiceId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InvoiceDTO> cancel(@PathVariable UUID hospitalId,
                                              @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.cancel(hospitalId, invoiceId));
    }
}
