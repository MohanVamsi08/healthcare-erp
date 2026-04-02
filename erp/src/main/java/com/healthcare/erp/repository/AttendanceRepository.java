package com.healthcare.erp.repository;

import com.healthcare.erp.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByHospitalIdAndDate(UUID hospitalId, LocalDate date);
    List<Attendance> findByStaffIdAndDateBetween(UUID staffId, LocalDate start, LocalDate end);
    Optional<Attendance> findByStaffIdAndDate(UUID staffId, LocalDate date);
    boolean existsByStaffIdAndDate(UUID staffId, LocalDate date);
}
