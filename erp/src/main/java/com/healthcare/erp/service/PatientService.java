package com.healthcare.erp.service;

import com.healthcare.erp.dto.PatientDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.Patient;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final HospitalRepository hospitalRepository;

    public List<PatientDTO> getByHospitalId(UUID hospitalId) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        return patientRepository.findByHospitalId(hospitalId)
                .stream()
                .map(PatientDTO::fromEntity)
                .toList();
    }

    public PatientDTO getById(UUID id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return PatientDTO.fromEntity(patient);
    }

    public PatientDTO create(UUID hospitalId, PatientDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Patient patient = Patient.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .dateOfBirth(dto.dateOfBirth())
                .gender(dto.gender())
                .phone(dto.phone())
                .email(dto.email())
                .aadhaarNumber(dto.aadhaarNumber())
                .bloodGroup(dto.bloodGroup())
                .hospital(hospital)
                .isActive(dto.isActive())
                .build();

        Patient saved = patientRepository.save(patient);
        return PatientDTO.fromEntity(saved);
    }

    public PatientDTO update(UUID id, PatientDTO dto) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        patient.setFirstName(dto.firstName());
        patient.setLastName(dto.lastName());
        patient.setDateOfBirth(dto.dateOfBirth());
        patient.setGender(dto.gender());
        patient.setPhone(dto.phone());
        patient.setEmail(dto.email());
        patient.setAadhaarNumber(dto.aadhaarNumber());
        patient.setBloodGroup(dto.bloodGroup());
        patient.setActive(dto.isActive());

        Patient updated = patientRepository.save(patient);
        return PatientDTO.fromEntity(updated);
    }

    public void delete(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient", id);
        }
        patientRepository.deleteById(id);
    }
}
