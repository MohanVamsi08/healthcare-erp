package com.healthcare.erp.controller;

import com.healthcare.erp.dto.report.*;
import com.healthcare.erp.service.ReportExportService;
import com.healthcare.erp.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hospitals/{hospitalId}/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

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

    // ────────────────────── EXPORT ENDPOINTS ──────────────────────

    @GetMapping("/dashboard/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportDashboardExcel(@PathVariable UUID hospitalId) throws IOException {
        byte[] data = reportExportService.exportDashboardExcel(hospitalId);
        return buildFileResponse(data, "dashboard_report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/dashboard/export/pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportDashboardPdf(@PathVariable UUID hospitalId) {
        byte[] data = reportExportService.exportDashboardPdf(hospitalId);
        return buildFileResponse(data, "dashboard_report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/revenue/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportRevenueExcel(@PathVariable UUID hospitalId) throws IOException {
        byte[] data = reportExportService.exportRevenueExcel(hospitalId);
        return buildFileResponse(data, "revenue_report.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/revenue/export/pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportRevenuePdf(@PathVariable UUID hospitalId) {
        byte[] data = reportExportService.exportRevenuePdf(hospitalId);
        return buildFileResponse(data, "revenue_report.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/doctors/workload/export/excel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportDoctorWorkloadExcel(@PathVariable UUID hospitalId) throws IOException {
        byte[] data = reportExportService.exportDoctorWorkloadExcel(hospitalId);
        return buildFileResponse(data, "doctor_workload.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/doctors/workload/export/pdf")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN') and @tenantGuard.canAccessTenant(authentication, #hospitalId)")
    public ResponseEntity<byte[]> exportDoctorWorkloadPdf(@PathVariable UUID hospitalId) {
        byte[] data = reportExportService.exportDoctorWorkloadPdf(hospitalId);
        return buildFileResponse(data, "doctor_workload.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] data, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(data);
    }
}

