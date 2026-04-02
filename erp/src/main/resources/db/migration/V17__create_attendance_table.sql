-- V17: Attendance tracking
CREATE TABLE attendance (
    id UUID PRIMARY KEY,
    staff_id UUID NOT NULL REFERENCES staff(id),
    hospital_id UUID NOT NULL REFERENCES hospitals(id),
    date DATE NOT NULL,
    clock_in TIME,
    clock_out TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    hours_worked DECIMAL(4,2),
    notes TEXT,
    UNIQUE(staff_id, date)
);

CREATE INDEX idx_attendance_hospital_date ON attendance(hospital_id, date);
CREATE INDEX idx_attendance_staff ON attendance(staff_id);
