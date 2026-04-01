CREATE TABLE record_transfer_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    from_hospital_id UUID NOT NULL REFERENCES hospitals(id),
    to_hospital_id UUID NOT NULL REFERENCES hospitals(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE patient_consents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    target_hospital_id UUID NOT NULL REFERENCES hospitals(id),
    consent_document TEXT,
    consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (patient_id, hospital_id, target_hospital_id)
);

CREATE INDEX idx_transfers_from_hospital ON record_transfer_requests(from_hospital_id);
CREATE INDEX idx_transfers_to_hospital ON record_transfer_requests(to_hospital_id);
CREATE INDEX idx_transfers_patient ON record_transfer_requests(patient_id);
CREATE INDEX idx_consents_hospital ON patient_consents(hospital_id);
CREATE INDEX idx_consents_patient ON patient_consents(patient_id);
CREATE INDEX idx_consents_target ON patient_consents(target_hospital_id);
