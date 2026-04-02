package com.healthcare.erp.controller;

import com.healthcare.erp.dto.AttendanceDTO;
import com.healthcare.erp.model.AttendanceStatus;
import com.healthcare.erp.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<AttendanceDTO>> getByDate(
            @PathVariable UUID hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByHospitalAndDate(hospitalId, date));
    }

    @GetMapping("/staff/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<AttendanceDTO>> getByStaff(
            @PathVariable UUID hospitalId,
            @PathVariable UUID staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(attendanceService.getByStaff(hospitalId, staffId, start, end));
    }

    @PostMapping("/clock-in/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'NURSE', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AttendanceDTO> clockIn(@PathVariable UUID hospitalId,
                                                  @PathVariable UUID staffId) {
        return ResponseEntity.ok(attendanceService.clockIn(hospitalId, staffId));
    }

    @PostMapping("/clock-out/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'NURSE', 'RECEPTIONIST') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AttendanceDTO> clockOut(@PathVariable UUID hospitalId,
                                                   @PathVariable UUID staffId) {
        return ResponseEntity.ok(attendanceService.clockOut(hospitalId, staffId));
    }

    @PostMapping("/mark-status/{staffId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<AttendanceDTO> markStatus(@PathVariable UUID hospitalId,
                                                     @PathVariable UUID staffId,
                                                     @RequestBody Map<String, String> body) {
        LocalDate date = LocalDate.parse(body.get("date"));
        AttendanceStatus status = AttendanceStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(attendanceService.markStatus(hospitalId, staffId, date, status));
    }
}
