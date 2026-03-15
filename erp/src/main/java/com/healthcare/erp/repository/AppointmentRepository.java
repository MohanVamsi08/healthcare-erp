package com.healthcare.erp.repository;

import com.healthcare.erp.model.Appointment;
import com.healthcare.erp.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByHospitalId(UUID hospitalId);
    List<Appointment> findByDoctorIdAndAppointmentDateTimeBetween(UUID doctorId, LocalDateTime start, LocalDateTime end);
    List<Appointment> findByPatientId(UUID patientId);
    List<Appointment> findByHospitalIdAndStatus(UUID hospitalId, AppointmentStatus status);
}
