package com.healthcare.erp.controller;

import com.healthcare.erp.dto.InsuranceClaimDTO;
import com.healthcare.erp.model.ClaimStatus;
import com.healthcare.erp.service.InsuranceClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/insurance-claims")
@RequiredArgsConstructor
public class InsuranceClaimController {

    private final InsuranceClaimService claimService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<InsuranceClaimDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(claimService.getByHospital(hospitalId));
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InsuranceClaimDTO> getById(@PathVariable UUID hospitalId,
                                                      @PathVariable UUID claimId) {
        return ResponseEntity.ok(claimService.getById(hospitalId, claimId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InsuranceClaimDTO> submit(@PathVariable UUID hospitalId,
                                                     @RequestBody InsuranceClaimDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimService.submit(hospitalId, dto));
    }

    @PatchMapping("/{claimId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<InsuranceClaimDTO> updateStatus(@PathVariable UUID hospitalId,
                                                           @PathVariable UUID claimId,
                                                           @RequestBody Map<String, String> body) {
        ClaimStatus newStatus = ClaimStatus.valueOf(body.get("status"));
        BigDecimal approvedAmount = body.containsKey("approvedAmount")
                ? new BigDecimal(body.get("approvedAmount")) : null;
        return ResponseEntity.ok(claimService.updateStatus(hospitalId, claimId, newStatus, approvedAmount));
    }
}
