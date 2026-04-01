package com.healthcare.erp.dto.report;

import java.util.Map;

public record PatientStatsDTO(
        long totalPatients,
        long activePatients,
        long inactivePatients,
        Map<String, Long> genderDistribution,
        Map<String, Long> bloodGroupDistribution
) {}
