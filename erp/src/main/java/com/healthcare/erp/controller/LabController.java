package com.healthcare.erp.controller;

import com.healthcare.erp.dto.LabOrderDTO;
import com.healthcare.erp.dto.LabTestDTO;
import com.healthcare.erp.model.LabOrderStatus;
import com.healthcare.erp.service.LabOrderService;
import com.healthcare.erp.service.LabTestService;
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
@RequestMapping("/api/hospitals/{hospitalId}/lab")
@RequiredArgsConstructor
public class LabController {

    private final LabTestService labTestService;
    private final LabOrderService labOrderService;

    // ─── Test Catalog ───

    @GetMapping("/tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<LabTestDTO>> getTests(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(labTestService.getByHospital(hospitalId));
    }

    @GetMapping("/tests/{testId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabTestDTO> getTest(@PathVariable UUID hospitalId, @PathVariable UUID testId) {
        return ResponseEntity.ok(labTestService.getById(hospitalId, testId));
    }

    @PostMapping("/tests")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabTestDTO> createTest(@PathVariable UUID hospitalId,
                                                  @Valid @RequestBody LabTestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labTestService.create(hospitalId, dto));
    }

    @PutMapping("/tests/{testId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabTestDTO> updateTest(@PathVariable UUID hospitalId,
                                                  @PathVariable UUID testId,
                                                  @Valid @RequestBody LabTestDTO dto) {
        return ResponseEntity.ok(labTestService.update(hospitalId, testId, dto));
    }

    // ─── Lab Orders ───

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<LabOrderDTO>> getOrders(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(labOrderService.getByHospital(hospitalId));
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabOrderDTO> getOrder(@PathVariable UUID hospitalId, @PathVariable UUID orderId) {
        return ResponseEntity.ok(labOrderService.getById(hospitalId, orderId));
    }

    @GetMapping("/orders/patient/{patientId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<LabOrderDTO>> getOrdersByPatient(@PathVariable UUID hospitalId,
                                                                  @PathVariable UUID patientId) {
        return ResponseEntity.ok(labOrderService.getByPatient(hospitalId, patientId));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabOrderDTO> createOrder(@PathVariable UUID hospitalId,
                                                     @Valid @RequestBody LabOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labOrderService.create(hospitalId, dto));
    }

    @PostMapping("/orders/{orderId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabOrderDTO> updateOrderStatus(@PathVariable UUID hospitalId,
                                                           @PathVariable UUID orderId,
                                                           @RequestBody Map<String, String> body) {
        LabOrderStatus status = LabOrderStatus.valueOf(body.get("status"));
        String result = body.get("result");
        String resultNotes = body.get("resultNotes");
        return ResponseEntity.ok(labOrderService.updateStatus(hospitalId, orderId, status, result, resultNotes));
    }

    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LabOrderDTO> cancelOrder(@PathVariable UUID hospitalId,
                                                     @PathVariable UUID orderId) {
        return ResponseEntity.ok(labOrderService.cancel(hospitalId, orderId));
    }
}
