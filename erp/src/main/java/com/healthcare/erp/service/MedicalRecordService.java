package com.healthcare.erp.service;

import com.healthcare.erp.dto.MedicalRecordDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
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

    public MedicalRecordDTO create(UUID hospitalId, MedicalRecordDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", dto.doctorId()));

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
            record.setAppointment(appt);
        }

        return MedicalRecordDTO.fromEntity(medicalRecordRepository.save(record));
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

        return MedicalRecordDTO.fromEntity(medicalRecordRepository.save(record));
    }
}
