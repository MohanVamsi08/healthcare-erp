package com.healthcare.erp.repository;

import com.healthcare.erp.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    List<Department> findByHospitalId(UUID hospitalId);

    List<Department> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
