package com.healthcare.erp.repository;

import com.healthcare.erp.model.SupplierReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, UUID> {
    List<SupplierReturn> findByHospitalId(UUID hospitalId);
}
