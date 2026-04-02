-- V15: Wards & Beds
CREATE TABLE wards (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    floor VARCHAR(20),
    total_beds INT NOT NULL DEFAULT 0,
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, hospital_id)
);

CREATE TABLE beds (
    id UUID PRIMARY KEY,
    bed_number VARCHAR(20) NOT NULL,
    ward_id UUID NOT NULL REFERENCES wards(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    patient_id UUID REFERENCES patients(id),
    assigned_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(bed_number, ward_id)
);

CREATE INDEX idx_beds_ward ON beds(ward_id);
CREATE INDEX idx_beds_hospital_status ON beds(hospital_id, status);
CREATE INDEX idx_beds_patient ON beds(patient_id);
CREATE INDEX idx_wards_hospital ON wards(hospital_id);
