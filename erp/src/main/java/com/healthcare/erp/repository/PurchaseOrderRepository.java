package com.healthcare.erp.repository;

import com.healthcare.erp.model.PurchaseOrder;
import com.healthcare.erp.model.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    List<PurchaseOrder> findByHospitalId(UUID hospitalId);
    List<PurchaseOrder> findByHospitalIdAndStatus(UUID hospitalId, PurchaseOrderStatus status);
}
