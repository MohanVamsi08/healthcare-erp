package com.healthcare.erp.repository;

import com.healthcare.erp.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface StaffRepository extends JpaRepository<Staff, UUID> {
    List<Staff> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    List<Staff> findByHospitalIdAndDepartmentId(UUID hospitalId, UUID departmentId);
    boolean existsByEmployeeIdAndHospitalId(String employeeId, UUID hospitalId);
}
