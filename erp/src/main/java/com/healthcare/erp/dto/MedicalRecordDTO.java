package com.healthcare.erp.dto;

import com.healthcare.erp.model.MedicalRecord;
import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalRecordDTO(
        UUID id,
        UUID appointmentId,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        UUID hospitalId,
        String diagnosis,
        String prescription,
        String notes,
        String testResults,
        LocalDateTime createdAt) {

    public static MedicalRecordDTO fromEntity(MedicalRecord record) {
        return new MedicalRecordDTO(
                record.getId(),
                record.getAppointment() != null ? record.getAppointment().getId() : null,
                record.getPatient().getId(),
                record.getPatient().getFirstName() + " " + record.getPatient().getLastName(),
                record.getDoctor().getId(),
                record.getDoctor().getFirstName() + " " + record.getDoctor().getLastName(),
                record.getHospital().getId(),
                record.getDiagnosis(),
                record.getPrescription(),
                record.getNotes(),
                record.getTestResults(),
                record.getCreatedAt());
    }
}
