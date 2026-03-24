package com.healthcare.erp.repository;

import com.healthcare.erp.model.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, UUID> {
}
