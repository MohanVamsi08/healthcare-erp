package com.healthcare.erp.repository;

import com.healthcare.erp.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    List<Supplier> findByHospitalId(UUID hospitalId);
    List<Supplier> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
