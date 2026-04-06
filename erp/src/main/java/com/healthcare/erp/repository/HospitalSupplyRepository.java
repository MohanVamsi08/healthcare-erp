package com.healthcare.erp.repository;

import com.healthcare.erp.model.HospitalSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface HospitalSupplyRepository extends JpaRepository<HospitalSupply, UUID> {
    List<HospitalSupply> findByHospitalId(UUID hospitalId);
    Page<HospitalSupply> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<HospitalSupply> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    List<HospitalSupply> findByHospitalIdAndStockQuantityLessThanEqual(UUID hospitalId, int reorderLevel);
}
