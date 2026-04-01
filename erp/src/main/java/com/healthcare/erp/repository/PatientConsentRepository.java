package com.healthcare.erp.repository;

import com.healthcare.erp.model.PatientConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientConsentRepository extends JpaRepository<PatientConsent, UUID> {
    List<PatientConsent> findByHospitalId(UUID hospitalId);
    List<PatientConsent> findByPatientId(UUID patientId);
    boolean existsByPatientIdAndHospitalIdAndTargetHospitalIdAndConsentGivenTrue(
            UUID patientId, UUID hospitalId, UUID targetHospitalId);
}
