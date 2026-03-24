package com.healthcare.erp.repository;

import com.healthcare.erp.model.SupplierReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SupplierReturnItemRepository extends JpaRepository<SupplierReturnItem, UUID> {
}
