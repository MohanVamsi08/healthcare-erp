-- V18: Payroll
CREATE TABLE payroll (
    id UUID PRIMARY KEY,
    staff_id UUID NOT NULL REFERENCES staff(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    pay_month INT NOT NULL,
    pay_year INT NOT NULL,
    base_salary DECIMAL(12,2) NOT NULL,
    allowances DECIMAL(12,2) NOT NULL DEFAULT 0,
    deductions DECIMAL(12,2) NOT NULL DEFAULT 0,
    net_salary DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    paid_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(staff_id, pay_month, pay_year)
);

CREATE INDEX idx_payroll_hospital ON payroll(hospital_id);
CREATE INDEX idx_payroll_staff ON payroll(staff_id);
CREATE INDEX idx_payroll_period ON payroll(hospital_id, pay_year, pay_month);
