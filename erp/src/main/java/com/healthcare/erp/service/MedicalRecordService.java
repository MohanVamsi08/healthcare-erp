package com.healthcare.erp.service;

import com.healthcare.erp.dto.MedicalRecordDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AuditService auditService;

    public MedicalRecordDTO create(UUID hospitalId, MedicalRecordDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.doctorId()));
        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Doctor does not belong to this hospital");
        }

        MedicalRecord record = MedicalRecord.builder()
                .patient(patient)
                .doctor(doctor)
                .hospital(hospital)
                .diagnosis(dto.diagnosis())
                .prescription(dto.prescription())
                .notes(dto.notes())
                .testResults(dto.testResults())
                .build();

        if (dto.appointmentId() != null) {
            Appointment appt = appointmentRepository.findById(dto.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment", dto.appointmentId()));
            if (!appt.getHospital().getId().equals(hospitalId)) {
                throw new IllegalArgumentException("Appointment does not belong to this hospital");
            }
            record.setAppointment(appt);
        }

        MedicalRecord saved = medicalRecordRepository.save(record);
        auditService.logCreate("MedicalRecord", saved.getId().toString(), hospitalId, null);
        return MedicalRecordDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecordDTO> getByPatient(UUID hospitalId, UUID patientId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return medicalRecordRepository.findByPatientIdAndHospitalId(patientId, hospitalId)
                .stream().map(MedicalRecordDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public MedicalRecordDTO getById(UUID hospitalId, UUID recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", recordId));
        if (!record.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("MedicalRecord", recordId);
        }
        auditService.logRead("MedicalRecord", recordId.toString(), hospitalId, null);
        return MedicalRecordDTO.fromEntity(record);
    }

    public MedicalRecordDTO update(UUID hospitalId, UUID recordId, MedicalRecordDTO dto) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", recordId));
        if (!record.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("MedicalRecord", recordId);
        }

        if (dto.diagnosis() != null) record.setDiagnosis(dto.diagnosis());
        if (dto.prescription() != null) record.setPrescription(dto.prescription());
        if (dto.notes() != null) record.setNotes(dto.notes());
        if (dto.testResults() != null) record.setTestResults(dto.testResults());
        record.setUpdatedAt(java.time.LocalDateTime.now());

        MedicalRecord saved = medicalRecordRepository.save(record);
        auditService.logUpdate("MedicalRecord", recordId.toString(), hospitalId, null);
        return MedicalRecordDTO.fromEntity(saved);
    }
}
