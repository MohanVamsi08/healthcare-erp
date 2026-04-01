package com.healthcare.erp.service;

import com.healthcare.erp.dto.PatientConsentDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.PatientConsent;
import com.healthcare.erp.model.Hospital;
import com.healthcare.erp.model.Patient;
import com.healthcare.erp.repository.PatientConsentRepository;
import com.healthcare.erp.repository.HospitalRepository;
import com.healthcare.erp.repository.PatientRepository;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientConsentService {

    private final PatientConsentRepository consentRepository;
    private final HospitalRepository hospitalRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public List<PatientConsentDTO> getByHospital(UUID hospitalId, boolean fullAccess) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital", hospitalId);
        }
        auditService.logRead("PatientConsent", "LIST", hospitalId, null);
        var mapper = fullAccess
                ? (java.util.function.Function<com.healthcare.erp.model.PatientConsent, PatientConsentDTO>) PatientConsentDTO::fromEntity
                : (java.util.function.Function<com.healthcare.erp.model.PatientConsent, PatientConsentDTO>) PatientConsentDTO::fromEntityRedacted;
        return consentRepository.findByHospitalId(hospitalId).stream().map(mapper).toList();
    }

    public PatientConsentDTO getById(UUID hospitalId, UUID id, boolean fullAccess) {
        PatientConsent p = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PatientConsent", id));
        if (!p.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("PatientConsent", id);
        }
        auditService.logRead("PatientConsent", id.toString(), hospitalId, null);
        return fullAccess ? PatientConsentDTO.fromEntity(p) : PatientConsentDTO.fromEntityRedacted(p);
    }

    public PatientConsentDTO create(UUID hospitalId, PatientConsentDTO dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));
        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", dto.patientId()));
        if (!patient.getHospital().getId().equals(hospitalId)) {
            throw new IllegalArgumentException("Patient does not belong to this hospital");
        }

        // Validate target hospital
        Hospital targetHospital = hospitalRepository.findById(dto.targetHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", dto.targetHospitalId()));
        if (dto.targetHospitalId().equals(hospitalId)) {
            throw new IllegalArgumentException("Target hospital must be different from the source hospital");
        }

        // Consent must be meaningful — document required, and consent must be affirmative
        if (!dto.consentGiven()) {
            throw new IllegalArgumentException("Consent must be given (consentGiven=true) to create a consent record");
        }
        if (dto.consentDocument() == null || dto.consentDocument().isBlank()) {
            throw new IllegalArgumentException("Consent document text is required when granting consent");
        }

        PatientConsent p = PatientConsent.builder()
                .patient(patient)
                .hospital(hospital)
                .targetHospital(targetHospital)
                .consentDocument(dto.consentDocument())
                .consentGiven(true)
                .build();

        PatientConsent saved = consentRepository.save(p);
        auditService.logCreate("PatientConsent", saved.getId().toString(), hospitalId,
                "target=" + dto.targetHospitalId());
        return PatientConsentDTO.fromEntity(saved);
    }

    public void delete(UUID hospitalId, UUID id) {
        PatientConsent p = consentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PatientConsent", id));
        if (!p.getHospital().getId().equals(hospitalId)) {
            throw new ResourceNotFoundException("PatientConsent", id);
        }
        consentRepository.delete(p);
        auditService.logDelete("PatientConsent", id.toString(), hospitalId, null);
    }
}
