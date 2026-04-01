package com.healthcare.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consent DTO with validation — consentDocument is required when consentGiven is true.
 * Document is only returned in full to HOSPITAL_ADMIN; others see [REDACTED].
 */
public record PatientConsentDTO(
        UUID id,
        @NotNull(message = "Patient ID is required")
        UUID patientId,
        UUID hospitalId,
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
                "[REDACTED]",
                p.isConsentGiven(),
                p.getCreatedAt()
        );
    }
}
