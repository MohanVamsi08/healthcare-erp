# Module 1: Core — Walkthrough

> **Status:** ✅ Complete
> **What it does:** Manages hospitals, departments, and patients — the foundation of the ERP.

---

## What Was Built

### 3 Entities (Database Tables)

| Entity | Table Name | Key Fields | Relationship |
|--------|-----------|------------|-------------|
| Hospital | `hospitals` | name, GSTIN, state_code, address | Top-level entity |
| Department | `departments` | name, code | Belongs to a Hospital |
| Patient | `patients` | firstName, lastName, DOB, gender, phone, email, aadhaar, bloodGroup | Belongs to a Hospital |

### 6 Layers of Code (Per Entity)

| Layer | Purpose | Example |
|-------|---------|---------|
| **Model** | Defines database table | `Hospital.java` → `hospitals` table |
| **Repository** | Talks to database | `HospitalRepository.java` → SQL queries |
| **Service** | Business logic | `HospitalService.java` → validates & processes |
| **Controller** | API endpoint | `HospitalController.java` → receives HTTP requests |
| **DTO** | API response shape | `HospitalDTO.java` → clean data for client |
| **Migration** | Creates table | `V1__create_hospital_table.sql` |

### How a Request Flows

```
Client sends HTTP request (e.g. GET /api/hospitals)
     ↓
Controller receives it → asks Service
     ↓
Service applies business logic → asks Repository
     ↓
Repository queries PostgreSQL → gets data
     ↓
DTO converts data → sends back clean JSON response
```

---

## API Endpoints

### Hospitals
| Method | URL | What It Does |
|--------|-----|-------------|
| GET | `/api/hospitals` | List all hospitals |
| GET | `/api/hospitals/{id}` | Get one hospital |
| POST | `/api/hospitals` | Create a hospital |
| PUT | `/api/hospitals/{id}` | Update a hospital |
| DELETE | `/api/hospitals/{id}` | Delete a hospital |

### Departments
| Method | URL | What It Does |
|--------|-----|-------------|
| GET | `/api/hospitals/{id}/departments` | List departments in a hospital |
| POST | `/api/hospitals/{id}/departments` | Create a department |
| PUT | `/api/hospitals/{id}/departments/{deptId}` | Update a department |
| DELETE | `/api/hospitals/{id}/departments/{deptId}` | Delete a department |

### Patients
| Method | URL | What It Does |
|--------|-----|-------------|
| GET | `/api/hospitals/{id}/patients` | List patients in a hospital |
| POST | `/api/hospitals/{id}/patients` | Register a patient |
| PUT | `/api/hospitals/{id}/patients/{patientId}` | Update a patient |
| DELETE | `/api/hospitals/{id}/patients/{patientId}` | Delete a patient |

---

## Example API Usage

### Create a Hospital
```json
POST /api/hospitals
{
  "name": "Apollo Hospital",
  "gstin": "29ABCDE1234F1Z5",
  "stateCode": "KA",
  "address": "Bangalore, Karnataka"
}
// Response: 201 Created with the hospital data + auto-generated UUID
```

### Register a Patient
```json
POST /api/hospitals/{hospitalId}/patients
{
  "firstName": "Rahul",
  "lastName": "Sharma",
  "dateOfBirth": "1990-05-15",
  "gender": "Male",
  "phone": "9876543210",
  "email": "rahul@example.com",
  "aadhaarNumber": "123456789012",
  "bloodGroup": "O+"
}
```

---

## Key Concepts Used

| Concept | What It Means |
|---------|--------------|
| **Lombok** | `@Getter`, `@Setter`, `@Builder` — auto-generates boilerplate code |
| **JPA/Hibernate** | Maps Java objects to database tables automatically |
| **Flyway** | Version-controlled database migrations (V1, V2, V3 scripts) |
| **DTOs** | "Safe" objects sent to clients — hides internal database fields |
| **Exception Handler** | Returns clean error messages instead of ugly Java stack traces |

---

## Files Created

```
erp/src/main/java/com/healthcare/erp/
├── model/Hospital.java, Department.java, Patient.java
├── repository/HospitalRepository.java, DepartmentRepository.java, PatientRepository.java
├── service/HospitalService.java, DepartmentService.java, PatientService.java
├── controller/HospitalController.java, DepartmentController.java, PatientController.java, RootController.java
├── dto/HospitalDTO.java, DepartmentDTO.java, PatientDTO.java
└── exception/GlobalExceptionHandler.java, ResourceNotFoundException.java, ErrorResponse.java

erp/src/main/resources/db/migration/
├── V1__create_hospital_table.sql
├── V2__create_department_table.sql
└── V3__create_patient_table.sql
```
