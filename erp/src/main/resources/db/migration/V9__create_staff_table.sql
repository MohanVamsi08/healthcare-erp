CREATE TABLE staff (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id UUID REFERENCES departments(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    user_id UUID UNIQUE REFERENCES users(id),
    phone VARCHAR(15),
    email VARCHAR(255),
    date_of_joining DATE NOT NULL,
    salary DECIMAL(12, 2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_staff_employee_id_hospital ON staff(employee_id, hospital_id);
