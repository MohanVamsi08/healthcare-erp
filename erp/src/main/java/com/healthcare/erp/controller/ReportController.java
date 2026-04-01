package com.healthcare.erp.controller;

import com.healthcare.erp.dto.report.*;
import com.healthcare.erp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<DashboardSummaryDTO> getDashboard(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(reportService.getDashboardSummary(hospitalId));
    }

    @GetMapping("/patients")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<PatientStatsDTO> getPatientStats(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(reportService.getPatientStats(hospitalId));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<RevenueReportDTO> getRevenueReport(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(reportService.getRevenueReport(hospitalId));
    }

    @GetMapping("/doctors/workload")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<DoctorWorkloadDTO>> getDoctorWorkloads(@PathVariable UUID hospitalId) {
        return ResponseEntity.ok(reportService.getDoctorWorkloads(hospitalId));
    }

    @GetMapping("/appointments/trends")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<List<AppointmentTrendDTO>> getAppointmentTrends(
            @PathVariable UUID hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.getAppointmentTrends(hospitalId, start, end));
    }
}
