package com.healthcare.erp.repository;

import com.healthcare.erp.model.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
    List<LabTest> findByHospitalId(UUID hospitalId);
    Page<LabTest> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<LabTest> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    boolean existsByTestCodeAndHospitalId(String testCode, UUID hospitalId);
}
