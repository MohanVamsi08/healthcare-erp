package com.healthcare.erp.dto;

import com.healthcare.erp.model.Appointment;
import com.healthcare.erp.model.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentDTO(
        UUID id,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        UUID hospitalId,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status,
        String reason,
        String notes,
        LocalDateTime createdAt) {

    public static AppointmentDTO fromEntity(Appointment appt) {
        return new AppointmentDTO(
                appt.getId(),
                appt.getPatient().getId(),
                appt.getPatient().getFirstName() + " " + appt.getPatient().getLastName(),
                appt.getDoctor().getId(),
                appt.getDoctor().getFirstName() + " " + appt.getDoctor().getLastName(),
                appt.getHospital().getId(),
                appt.getAppointmentDateTime(),
                appt.getStatus(),
                appt.getReason(),
                appt.getNotes(),
                appt.getCreatedAt());
    }
}
