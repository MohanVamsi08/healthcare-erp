# Module 3: Clinical Operations — Walkthrough

> **Status:** ✅ Complete
> **What it does:** Doctors, Appointments, and Medical Records

---

## What Was Built

### 3 New Entities

| Entity | Table | What It Is |
|--------|-------|-----------|
| **Doctor** | `doctors` | A doctor at a hospital, linked to department and user account |
| **Appointment** | `appointments` | A scheduled visit between patient ↔ doctor |
| **MedicalRecord** | `medical_records` | Diagnosis, prescription, notes from a visit |

### Appointment Status Flow

```
SCHEDULED → CONFIRMED → IN_PROGRESS → COMPLETED
                                     → CANCELLED
                                     → NO_SHOW
```

---

## New API Endpoints

### Doctors
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/doctors` | ADMIN |
| GET | `/api/hospitals/{id}/doctors` | Any authenticated |
| GET | `/api/hospitals/{id}/doctors/{doctorId}` | Any authenticated |
| PUT | `/api/hospitals/{id}/doctors/{doctorId}` | ADMIN |
| DELETE | `/api/hospitals/{id}/doctors/{doctorId}` | ADMIN |

### Appointments
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/appointments` | ADMIN, RECEPTIONIST, DOCTOR |
| GET | `/api/hospitals/{id}/appointments` | ADMIN, DOCTOR, RECEPTIONIST |
| GET | `/api/hospitals/{id}/appointments/{apptId}` | Authenticated |
| PUT | `/api/hospitals/{id}/appointments/{apptId}` | ADMIN, DOCTOR, RECEPTIONIST |
| PATCH | `/api/hospitals/{id}/appointments/{apptId}/status` | ADMIN, DOCTOR |

### Medical Records
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/medical-records` | DOCTOR |
| GET | `/api/hospitals/{id}/patients/{patientId}/medical-records` | DOCTOR, NURSE |
| GET | `/api/hospitals/{id}/medical-records/{recordId}` | DOCTOR, NURSE |
| PUT | `/api/hospitals/{id}/medical-records/{recordId}` | DOCTOR |

---

## Files Created (18 new)

```
model/Doctor.java, Appointment.java, AppointmentStatus.java, MedicalRecord.java
repository/DoctorRepository.java, AppointmentRepository.java, MedicalRecordRepository.java
service/DoctorService.java, AppointmentService.java, MedicalRecordService.java
controller/DoctorController.java, AppointmentController.java, MedicalRecordController.java
dto/DoctorDTO.java, AppointmentDTO.java, MedicalRecordDTO.java
db/migration/V5, V6, V7 SQL files
```

---

## Bugs Fixed During Build
- `boolean` → `Boolean` in ALL DTOs (PatientDTO, DepartmentDTO, UserDTO, DoctorDTO) to handle null JSON
- Added `-parameters` compiler flag to `pom.xml` for `@PathVariable` resolution
- Fixed DB credentials (`postgres/postgres`) for fresh Docker container

## Test Results

| # | Test | Result |
|---|------|--------|
| 1 | Login as SUPER_ADMIN | ✅ |
| 2 | Register Doctor | ✅ |
| 3 | Register Patient | ✅ |
| 4 | Book Appointment | ✅ |
| 5 | Complete Appointment | ✅ COMPLETED |
| 6 | Create Medical Record | ✅ |
| 7 | Query Patient Records | ✅ 1 record found |
