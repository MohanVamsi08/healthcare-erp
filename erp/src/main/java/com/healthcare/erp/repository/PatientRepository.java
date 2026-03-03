package com.healthcare.erp.repository;

import com.healthcare.erp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByHospitalId(UUID hospitalId);

    List<Patient> findByHospitalIdAndIsActiveTrue(UUID hospitalId);
}
