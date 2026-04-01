package com.healthcare.erp.dto.report;

import java.time.LocalDate;

public record AppointmentTrendDTO(
        LocalDate date,
        long count
) {}
