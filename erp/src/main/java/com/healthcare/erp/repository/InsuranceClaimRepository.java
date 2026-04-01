package com.healthcare.erp.repository;

import com.healthcare.erp.model.ClaimStatus;
import com.healthcare.erp.model.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {
    List<InsuranceClaim> findByHospitalId(UUID hospitalId);
    List<InsuranceClaim> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    List<InsuranceClaim> findByHospitalIdAndStatus(UUID hospitalId, ClaimStatus status);
    long countByHospitalIdAndStatus(UUID hospitalId, ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.claimedAmount), 0) FROM InsuranceClaim c WHERE c.hospital.id = :hospitalId")
    BigDecimal sumClaimedAmountByHospitalId(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT COALESCE(SUM(c.approvedAmount), 0) FROM InsuranceClaim c WHERE c.hospital.id = :hospitalId AND c.status = 'APPROVED'")
    BigDecimal sumApprovedAmountByHospitalId(@Param("hospitalId") UUID hospitalId);
}
