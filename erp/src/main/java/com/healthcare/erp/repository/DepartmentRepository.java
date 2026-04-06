package com.healthcare.erp.repository;

import com.healthcare.erp.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByHospitalId(UUID hospitalId);
    Page<Department> findByHospitalId(UUID hospitalId, Pageable pageable);

    List<Department> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
