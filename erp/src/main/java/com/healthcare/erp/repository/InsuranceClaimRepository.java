package com.healthcare.erp.repository;

import com.healthcare.erp.model.ClaimStatus;
import com.healthcare.erp.model.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {
    List<InsuranceClaim> findByHospitalId(UUID hospitalId);
    List<InsuranceClaim> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    List<InsuranceClaim> findByHospitalIdAndStatus(UUID hospitalId, ClaimStatus status);
}
