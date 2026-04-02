package com.healthcare.erp.dto;

import com.healthcare.erp.model.Attendance;
import com.healthcare.erp.model.AttendanceStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AttendanceDTO(
        UUID id,
        @NotNull UUID staffId,
        UUID hospitalId,
        @NotNull LocalDate date,
        LocalTime clockIn,
        LocalTime clockOut,
        AttendanceStatus status,
        BigDecimal hoursWorked,
        String notes
) {
    public static AttendanceDTO fromEntity(Attendance a) {
        return new AttendanceDTO(
                a.getId(),
                a.getStaff().getId(),
                a.getHospital().getId(),
                a.getDate(),
                a.getClockIn(),
                a.getClockOut(),
                a.getStatus(),
                a.getHoursWorked(),
                a.getNotes()
        );
    }
}
