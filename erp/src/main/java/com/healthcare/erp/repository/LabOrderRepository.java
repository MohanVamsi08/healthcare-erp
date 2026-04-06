package com.healthcare.erp.repository;

import com.healthcare.erp.model.LabOrder;
import com.healthcare.erp.model.LabOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Repository
public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {
    List<LabOrder> findByHospitalId(UUID hospitalId);
    Page<LabOrder> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<LabOrder> findByHospitalIdAndStatus(UUID hospitalId, LabOrderStatus status);
    List<LabOrder> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
}
