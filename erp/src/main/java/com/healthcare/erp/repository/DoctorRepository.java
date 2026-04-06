package com.healthcare.erp.repository;

import com.healthcare.erp.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    List<Doctor> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    Page<Doctor> findByHospitalIdAndIsActiveTrue(UUID hospitalId, Pageable pageable);
    List<Doctor> findByDepartmentId(UUID departmentId);
    boolean existsByLicenseNumber(String licenseNumber);
    java.util.Optional<Doctor> findByUserId(UUID userId);
    long countByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
