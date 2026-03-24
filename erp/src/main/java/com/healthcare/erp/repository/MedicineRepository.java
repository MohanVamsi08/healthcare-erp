package com.healthcare.erp.repository;

import com.healthcare.erp.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    List<Medicine> findByHospitalId(UUID hospitalId);
    List<Medicine> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
    List<Medicine> findByHospitalIdAndStockQuantityLessThanEqual(UUID hospitalId, int reorderLevel);
    List<Medicine> findByHospitalIdAndExpiryDateBefore(UUID hospitalId, LocalDate date);
}
