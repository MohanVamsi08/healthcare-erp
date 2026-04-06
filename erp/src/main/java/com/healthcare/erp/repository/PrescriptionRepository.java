package com.healthcare.erp.repository;

import com.healthcare.erp.model.Prescription;
import com.healthcare.erp.model.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByHospitalId(UUID hospitalId);
    Page<Prescription> findByHospitalId(UUID hospitalId, Pageable pageable);
    List<Prescription> findByHospitalIdAndStatus(UUID hospitalId, PrescriptionStatus status);
    List<Prescription> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    long countByHospitalId(UUID hospitalId);
    long countByDoctorIdAndHospitalId(UUID doctorId, UUID hospitalId);
}
