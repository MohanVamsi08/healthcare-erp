package com.healthcare.erp.repository;

import com.healthcare.erp.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    List<MedicalRecord> findByPatientIdAndHospitalId(UUID patientId, UUID hospitalId);
    List<MedicalRecord> findByPatientId(UUID patientId);
    List<MedicalRecord> findByDoctorId(UUID doctorId);
}
