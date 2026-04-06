package com.healthcare.erp.repository;

import com.healthcare.erp.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByHospitalId(UUID hospitalId);
    Page<Supplier> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<Supplier> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
