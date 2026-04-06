package com.healthcare.erp.repository;

import com.healthcare.erp.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    List<Staff> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    Page<Staff> findByHospitalIdAndIsActiveTrue(UUID hospitalId, Pageable pageable);
    List<Staff> findByHospitalIdAndDepartmentId(UUID hospitalId, UUID departmentId);
    boolean existsByEmployeeIdAndHospitalId(String employeeId, UUID hospitalId);
    long countByHospitalIdAndIsActiveTrue(UUID hospitalId);
    java.util.Optional<Staff> findByUserId(UUID userId);
}
