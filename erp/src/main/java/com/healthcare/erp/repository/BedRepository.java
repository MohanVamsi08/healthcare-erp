package com.healthcare.erp.repository;

import com.healthcare.erp.model.Bed;
import com.healthcare.erp.model.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BedRepository extends JpaRepository<Bed, UUID> {
    List<Bed> findByWardId(UUID wardId);
    List<Bed> findByHospitalIdAndStatus(UUID hospitalId, BedStatus status);
    long countByWardIdAndStatus(UUID wardId, BedStatus status);
    long countByHospitalIdAndStatus(UUID hospitalId, BedStatus status);
    boolean existsByBedNumberAndWardId(String bedNumber, UUID wardId);
    boolean existsByPatientIdAndStatus(UUID patientId, BedStatus status);
}
