package com.healthcare.erp.repository;

import com.healthcare.erp.model.Appointment;
import com.healthcare.erp.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    long countByHospitalId(UUID hospitalId);
    long countByHospitalIdAndStatus(UUID hospitalId, AppointmentStatus status);
    long countByDoctorIdAndHospitalId(UUID doctorId, UUID hospitalId);
    long countByDoctorIdAndHospitalIdAndStatus(UUID doctorId, UUID hospitalId, AppointmentStatus status);

    @Query("SELECT CAST(a.appointmentDateTime AS LocalDate), COUNT(a) FROM Appointment a " +
           "WHERE a.hospital.id = :hospitalId AND a.appointmentDateTime BETWEEN :start AND :end " +
           "GROUP BY CAST(a.appointmentDateTime AS LocalDate) ORDER BY CAST(a.appointmentDateTime AS LocalDate)")
    List<Object[]> countByHospitalIdGroupByDate(@Param("hospitalId") UUID hospitalId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);
}
