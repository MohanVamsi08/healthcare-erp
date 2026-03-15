package com.healthcare.erp.dto;

import com.healthcare.erp.model.LeaveRequest;
import com.healthcare.erp.model.LeaveType;
import com.healthcare.erp.model.LeaveStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveRequestDTO(
        UUID id,
        UUID staffId,
        String staffName,
        UUID hospitalId,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        String reason,
        LeaveStatus status,
        String approvedByEmail,
        LocalDateTime createdAt) {

    public static LeaveRequestDTO fromEntity(LeaveRequest lr) {
        return new LeaveRequestDTO(
                lr.getId(),
                lr.getStaff().getId(),
                lr.getStaff().getFirstName() + " " + lr.getStaff().getLastName(),
                lr.getHospital().getId(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getReason(),
                lr.getStatus(),
                lr.getApprovedBy() != null ? lr.getApprovedBy().getEmail() : null,
                lr.getCreatedAt());
    }
}
