package com.healthcare.erp.controller;

import com.healthcare.erp.dto.LeaveRequestDTO;
import com.healthcare.erp.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.healthcare.erp.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<LeaveRequestDTO>> getAll(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(leaveRequestService.getByHospitalId(hospitalId));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<LeaveRequestDTO>> getPending(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(leaveRequestService.getPending(hospitalId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LeaveRequestDTO> create(@PathVariable UUID hospitalId,
                                                   @RequestBody LeaveRequestDTO dto) {
        return ResponseEntity.ok(leaveRequestService.create(hospitalId, dto));
    }

    @PatchMapping("/{leaveId}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LeaveRequestDTO> approve(@PathVariable UUID hospitalId,
                                                    @PathVariable UUID leaveId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow().getId();
        return ResponseEntity.ok(leaveRequestService.approve(hospitalId, leaveId, userId));
    }

    @PatchMapping("/{leaveId}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<LeaveRequestDTO> reject(@PathVariable UUID hospitalId,
                                                   @PathVariable UUID leaveId) {
        return ResponseEntity.ok(leaveRequestService.reject(hospitalId, leaveId));
    }
}
