package com.healthcare.erp.controller;

import com.healthcare.erp.dto.StockTransactionDTO;
import com.healthcare.erp.service.StockTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/stock-transactions")
@RequiredArgsConstructor
public class StockTransactionController {

    private final StockTransactionService txnService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<StockTransactionDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(txnService.getByHospital(hospitalId));
    }

    @GetMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<StockTransactionDTO>> getByMedicine(@PathVariable UUID hospitalId,
                                                                     @PathVariable UUID medicineId) {
        return ResponseEntity.ok(txnService.getByMedicine(hospitalId, medicineId));
    }

    @GetMapping("/supply/{supplyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<StockTransactionDTO>> getBySupply(@PathVariable UUID hospitalId,
                                                                   @PathVariable UUID supplyId) {
        return ResponseEntity.ok(txnService.getBySupply(hospitalId, supplyId));
    }
}
