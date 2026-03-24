package com.healthcare.erp.dto;

import com.healthcare.erp.model.Prescription;
import com.healthcare.erp.model.PrescriptionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PrescriptionDTO(
        UUID id,
        String prescriptionNumber,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        String patientName,
        @NotNull(message = "Doctor ID is required")
        UUID doctorId,
        String doctorName,
        UUID hospitalId,
        PrescriptionStatus status,
        String notes,
        LocalDateTime prescribedAt,
        LocalDateTime dispensedAt,
        @NotEmpty(message = "At least one prescription item is required")
        @Valid
        List<PrescriptionItemDTO> items
) {
    public static PrescriptionDTO fromEntity(Prescription p) {
        List<PrescriptionItemDTO> itemDTOs = p.getItems().stream()
                .map(item -> new PrescriptionItemDTO(
                        item.getId(), item.getMedicine().getId(),
                        item.getMedicine().getName(), item.getQuantity(),
                        item.getDosageInstructions()))
                .toList();
        return new PrescriptionDTO(p.getId(), p.getPrescriptionNumber(),
                p.getPatient().getId(),
                p.getPatient().getFirstName() + " " + p.getPatient().getLastName(),
                p.getDoctor().getId(),
                p.getDoctor().getFirstName() + " " + p.getDoctor().getLastName(),
                p.getHospital().getId(), p.getStatus(), p.getNotes(),
                p.getPrescribedAt(), p.getDispensedAt(), itemDTOs);
    }
}
