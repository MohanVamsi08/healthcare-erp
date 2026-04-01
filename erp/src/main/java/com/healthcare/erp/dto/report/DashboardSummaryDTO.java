package com.healthcare.erp.dto.report;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
        long totalPatients,
        long activePatients,
        long totalDoctors,
        long totalStaff,
        long totalAppointments,
        long pendingAppointments,
        long completedAppointments,
        long cancelledAppointments,
        BigDecimal totalRevenue,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        long lowStockMedicines,
        long totalPrescriptions,
        long totalInvoices
) {}
