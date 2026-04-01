package com.healthcare.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consent DTO — consent is per-destination-hospital.
 * consentDocument is only returned in full to HOSPITAL_ADMIN; others see [REDACTED].
 */
public record PatientConsentDTO(
        UUID id,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        UUID hospitalId,
        @NotNull(message = "Target hospital ID is required")
        UUID targetHospitalId,
        @NotBlank(message = "Consent document text is required")
        String consentDocument,
        boolean consentGiven,
        LocalDateTime createdAt
) {
    public static PatientConsentDTO fromEntity(com.healthcare.erp.model.PatientConsent p) {
        return new PatientConsentDTO(
                p.getId(),
                p.getPatient().getId(),
                p.getHospital().getId(),
                p.getTargetHospital().getId(),
                p.getConsentDocument(),
                p.isConsentGiven(),
                p.getCreatedAt()
        );
    }

    public static PatientConsentDTO fromEntityRedacted(com.healthcare.erp.model.PatientConsent p) {
        return new PatientConsentDTO(
                p.getId(),
                p.getPatient().getId(),
                p.getHospital().getId(),
                p.getTargetHospital().getId(),
                "[REDACTED]",
                p.isConsentGiven(),
                p.getCreatedAt()
        );
    }
}
