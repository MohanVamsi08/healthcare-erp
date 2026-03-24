package com.healthcare.erp.repository;

import com.healthcare.erp.model.PurchaseOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
}
