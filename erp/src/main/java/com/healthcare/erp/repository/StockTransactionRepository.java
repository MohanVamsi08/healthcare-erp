package com.healthcare.erp.repository;

import com.healthcare.erp.model.StockTransaction;
import com.healthcare.erp.model.StockTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    List<StockTransaction> findByHospitalId(UUID hospitalId);
    List<StockTransaction> findByMedicineId(UUID medicineId);
    List<StockTransaction> findBySupplyId(UUID supplyId);
    List<StockTransaction> findByHospitalIdAndTransactionType(UUID hospitalId, StockTransactionType type);
}
