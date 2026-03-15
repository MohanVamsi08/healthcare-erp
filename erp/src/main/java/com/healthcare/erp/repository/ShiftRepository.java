package com.healthcare.erp.repository;

import com.healthcare.erp.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findByHospitalIdAndShiftDate(UUID hospitalId, LocalDate date);
    List<Shift> findByStaffIdAndShiftDateBetween(UUID staffId, LocalDate start, LocalDate end);
    List<Shift> findByHospitalIdAndStaffId(UUID hospitalId, UUID staffId);
}
