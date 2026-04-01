package com.healthcare.erp.dto.report;

import java.math.BigDecimal;
import java.util.Map;

public record RevenueReportDTO(
        BigDecimal totalRevenue,
        BigDecimal totalGst,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        Map<String, Long> invoicesByStatus,
        Map<String, Long> claimsByStatus,
        BigDecimal totalClaimedAmount,
        BigDecimal totalApprovedAmount
) {}
