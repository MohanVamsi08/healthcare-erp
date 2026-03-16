package com.healthcare.erp.controller;

import com.healthcare.erp.dto.PaymentDTO;
import com.healthcare.erp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/invoices/{invoiceId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<PaymentDTO>> getByInvoice(@PathVariable UUID hospitalId,
                                                          @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(paymentService.getByInvoice(hospitalId, invoiceId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PaymentDTO> recordPayment(@PathVariable UUID hospitalId,
                                                     @PathVariable UUID invoiceId,
                                                     @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.recordPayment(hospitalId, invoiceId, dto));
    }
}
