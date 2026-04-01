package com.healthcare.erp.repository;

import com.healthcare.erp.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByHospitalId(UUID hospitalId);

    List<Patient> findByHospitalIdAndIsActiveTrue(UUID hospitalId);

    long countByHospitalId(UUID hospitalId);

    long countByHospitalIdAndIsActiveTrue(UUID hospitalId);

    long countByHospitalIdAndIsActiveFalse(UUID hospitalId);

    @Query("SELECT p.gender, COUNT(p) FROM Patient p WHERE p.hospital.id = :hospitalId GROUP BY p.gender")
    List<Object[]> countByHospitalIdGroupByGender(@Param("hospitalId") UUID hospitalId);

    @Query("SELECT p.bloodGroup, COUNT(p) FROM Patient p WHERE p.hospital.id = :hospitalId GROUP BY p.bloodGroup")
    List<Object[]> countByHospitalIdGroupByBloodGroup(@Param("hospitalId") UUID hospitalId);
}
