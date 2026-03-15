package com.healthcare.erp.dto;

import com.healthcare.erp.model.Shift;
import com.healthcare.erp.model.ShiftType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShiftDTO(
        UUID id,
        UUID staffId,
        String staffName,
        UUID hospitalId,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        ShiftType shiftType,
        String status,
        String notes,
        LocalDateTime createdAt) {

    public static ShiftDTO fromEntity(Shift shift) {
        return new ShiftDTO(
                shift.getId(),
                shift.getStaff().getId(),
                shift.getStaff().getFirstName() + " " + shift.getStaff().getLastName(),
                shift.getHospital().getId(),
                shift.getShiftDate(),
                shift.getStartTime(),
                shift.getEndTime(),
                shift.getShiftType(),
                shift.getStatus(),
                shift.getNotes(),
                shift.getCreatedAt());
    }
}
