package com.healthcare.erp.dto.report;

import java.util.UUID;

public record DoctorWorkloadDTO(
        UUID doctorId,
        String doctorName,
        String specialization,
        long totalAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long totalPrescriptions
) {}
