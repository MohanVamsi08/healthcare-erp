package com.healthcare.erp.repository;

import com.healthcare.erp.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    List<Doctor> findByDepartmentId(UUID departmentId);
    boolean existsByLicenseNumber(String licenseNumber);
}
