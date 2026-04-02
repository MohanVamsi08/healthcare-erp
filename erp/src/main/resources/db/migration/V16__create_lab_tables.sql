-- V16: Lab Tests & Orders
CREATE TABLE lab_tests (
    id UUID PRIMARY KEY,
    test_name VARCHAR(100) NOT NULL,
    test_code VARCHAR(20) NOT NULL UNIQUE,
    category VARCHAR(20) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lab_orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    patient_id UUID NOT NULL REFERENCES patients(id),
    doctor_id UUID NOT NULL REFERENCES doctors(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    lab_test_id UUID NOT NULL REFERENCES lab_tests(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ORDERED',
    result TEXT,
    result_notes TEXT,
    ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_lab_tests_hospital ON lab_tests(hospital_id);
CREATE INDEX idx_lab_orders_hospital ON lab_orders(hospital_id);
CREATE INDEX idx_lab_orders_patient ON lab_orders(patient_id);
CREATE INDEX idx_lab_orders_doctor ON lab_orders(doctor_id);
CREATE INDEX idx_lab_orders_status ON lab_orders(hospital_id, status);
