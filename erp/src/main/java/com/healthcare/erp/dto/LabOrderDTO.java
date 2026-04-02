package com.healthcare.erp.dto;

import com.healthcare.erp.model.LabOrder;
import com.healthcare.erp.model.LabOrderStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record LabOrderDTO(
        UUID id,
        String orderNumber,
        @NotNull UUID patientId,
        @NotNull UUID doctorId,
        UUID hospitalId,
        @NotNull UUID labTestId,
        LabOrderStatus status,
        String result,
        String resultNotes,
        LocalDateTime orderedAt,
        LocalDateTime completedAt
) {
    public static LabOrderDTO fromEntity(LabOrder order) {
        return new LabOrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getPatient().getId(),
                order.getDoctor().getId(),
                order.getHospital().getId(),
                order.getLabTest().getId(),
                order.getStatus(),
                order.getResult(),
                order.getResultNotes(),
                order.getOrderedAt(),
                order.getCompletedAt()
        );
    }
}
