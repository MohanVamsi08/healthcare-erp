-- Flyway migration: create patient_consents and record_transfer_requests tables
CREATE TABLE IF NOT EXISTS patient_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    hospital_id UUID NOT NULL,
    consent_document TEXT,
    consent_given BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    CONSTRAINT fk_patient_consents_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_patient_consents_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
);

CREATE TABLE IF NOT EXISTS record_transfer_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL,
    from_hospital_id UUID NOT NULL,
    to_hospital_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),
    CONSTRAINT fk_transfer_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_transfer_from_hospital FOREIGN KEY (from_hospital_id) REFERENCES hospitals(id),
    CONSTRAINT fk_transfer_to_hospital FOREIGN KEY (to_hospital_id) REFERENCES hospitals(id)
);
