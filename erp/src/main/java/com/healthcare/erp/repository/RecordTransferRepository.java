package com.healthcare.erp.repository;

import com.healthcare.erp.model.RecordTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecordTransferRepository extends JpaRepository<RecordTransferRequest, UUID> {
    List<RecordTransferRequest> findByFromHospitalId(UUID hospitalId);
    List<RecordTransferRequest> findByToHospitalId(UUID hospitalId);
}
