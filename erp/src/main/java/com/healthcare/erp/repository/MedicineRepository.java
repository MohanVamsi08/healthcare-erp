package com.healthcare.erp.repository;

import com.healthcare.erp.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    List<Medicine> findByHospitalId(UUID hospitalId);
    Page<Medicine> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<Medicine> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    List<Medicine> findByHospitalIdAndStockQuantityLessThanEqual(UUID hospitalId, int reorderLevel);
    List<Medicine> findByHospitalIdAndExpiryDateBefore(UUID hospitalId, LocalDate date);
}
