package com.healthcare.erp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

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
}
