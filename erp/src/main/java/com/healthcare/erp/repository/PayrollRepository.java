package com.healthcare.erp.repository;

import com.healthcare.erp.model.Payroll;
import com.healthcare.erp.model.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    List<Payroll> findByHospitalIdAndMonthAndYear(UUID hospitalId, int month, int year);
    List<Payroll> findByHospitalIdAndStatus(UUID hospitalId, PayrollStatus status);
    List<Payroll> findByStaffIdAndYear(UUID staffId, int year);
    boolean existsByStaffIdAndMonthAndYear(UUID staffId, int month, int year);
}
