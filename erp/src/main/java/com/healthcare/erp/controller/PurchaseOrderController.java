package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PurchaseOrderDTO;
import com.healthcare.erp.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<Page<PurchaseOrderDTO>> getAll(@PathVariable UUID hospitalId, Pageable pageable) {
        return ResponseEntity.ok(poService.getByHospital(hospitalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PurchaseOrderDTO> getById(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(poService.getById(hospitalId, id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PurchaseOrderDTO> create(@PathVariable UUID hospitalId,
                                                    @RequestBody @Valid PurchaseOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(poService.create(hospitalId, dto));
    }

    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PurchaseOrderDTO> markDelivered(@PathVariable UUID hospitalId,
                                                           @PathVariable UUID id,
                                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(poService.markDelivered(hospitalId, id,
                body.get("deliveredBy"), body.get("deliveryLocation")));
    }

    @PatchMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PurchaseOrderDTO> receive(@PathVariable UUID hospitalId,
                                                     @PathVariable UUID id,
                                                     @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(poService.receive(hospitalId, id, body.get("receivedBy")));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PurchaseOrderDTO> cancel(@PathVariable UUID hospitalId, @PathVariable UUID id) {
        return ResponseEntity.ok(poService.cancel(hospitalId, id));
    }
}
