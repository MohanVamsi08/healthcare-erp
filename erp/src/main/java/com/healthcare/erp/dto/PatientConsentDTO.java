package com.healthcare.erp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consent DTO — consentDocument is only returned to HOSPITAL_ADMIN.
 * For other roles, only the consent status (given/not given) is visible.
 */
public record PatientConsentDTO(
        UUID id,
        UUID patientId,
        UUID hospitalId,
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

    /**
     * Returns a redacted DTO that hides the consent document content.
     * Used for roles that need to see consent status but not the full document.
     */
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
