# Healthcare ERP — Project Vision & Documentation

---

## 🎯 The Big Idea

Imagine **SAP**, but built exclusively for hospitals.

You're building a platform where **any hospital can sign up and get their own ERP system** — their own patient records, departments, staff management, billing, pharmacy — everything they need to run their hospital digitally. And each hospital's data is **completely separate** from every other hospital.

Think of it like **Gmail**: millions of people use Gmail, but your emails are yours — nobody else can see them. Your ERP works the same way: hundreds of hospitals use your platform, but Hospital A can never see Hospital B's data.

### What Makes This Special?

| Traditional Software | Your Healthcare ERP |
|---------------------|-------------------|
| Each hospital buys and installs their own software | You provide one platform, all hospitals use it |
| Hospitals manage their own servers | You manage everything (cloud/SaaS) |
| No communication between hospitals | Hospitals can **request medical records** from each other |
| Generic ERP (SAP, Oracle) adapted for healthcare | **Purpose-built** for hospitals from the ground up |

---

## 🏗️ How It Works (In Plain English)

### The Building Analogy

Think of your ERP like a **luxury apartment building**:

- **The building** = Your ERP platform (the code, the server)
- **Each apartment** = One hospital's space (their data, their settings)
- **The lobby** = Shared services (the login page, the platform itself)
- **Apartment walls** = Data isolation (Hospital A can't walk into Hospital B's apartment)
- **The intercom** = Medical record transfer (Hospital A can *request* info from Hospital B, but B has to *approve* it)

### Data Isolation

Every piece of data (patient, department, bill) has a `hospital_id` field. When an Apollo doctor searches for patients, the system automatically filters: *"Only show patients where hospital_id = Apollo's ID."* It's impossible for them to see Fortis's patients.

---

## 👥 Role Hierarchy

```
YOU (ERP Platform Owner)
  └── SUPER_ADMIN role
        ├── Creates hospitals
        ├── Creates a HOSPITAL_ADMIN for each hospital
        └── Has god-mode access to everything

HOSPITAL_ADMIN (one per hospital)
  ├── Creates user accounts for their hospital staff
  ├── Assigns roles: DOCTOR, NURSE, RECEPTIONIST, BILLING_STAFF, PHARMACIST
  ├── Can only manage users within THEIR hospital
  └── Cannot see other hospitals' data

STAFF (Doctor, Nurse, etc.)
  ├── Can only access data within their hospital
  └── Can only do what their role allows
```

**There is NO open registration.** Users cannot sign up on their own. The SUPER_ADMIN creates hospital admins, and hospital admins create staff accounts.

---

## 📦 All Modules

| Module | Status | Priority | Description |
|--------|--------|----------|-------------|
| Core | ✅ Built | — | Hospitals, Departments, Patients |
| Admin & Security | 🔲 Next | 🔴 High | Users, JWT Auth, RBAC |
| Clinical | 🔲 Planned | 🔴 High | Doctors, Appointments, Medical Records, Wards |
| Billing & Finance | 🔲 Planned | 🟡 Medium | Invoices, GST, Insurance, Payments |
| HR | 🔲 Planned | 🟡 Medium | Staff, Shifts, Attendance, Payroll |
| Pharmacy & Inventory | 🔲 Planned | 🟡 Medium | Medicines, Prescriptions, Stock |
| Medical Record Transfer | 🔲 Planned | 🟢 Later | Inter-hospital records, Consent |
| Reporting | 🔲 Planned | 🟢 Later | Dashboards, Analytics, Exports |
| Frontend | 🔲 Planned | 🟢 Later | Web Dashboard, Patient Portal |

---

## 🔒 Data Security (Critical)

| Security Layer | What It Does |
|---------------|-------------|
| **Encryption at Rest** | All patient data encrypted in the database |
| **Encryption in Transit** | HTTPS/TLS — data encrypted while traveling over the network |
| **RBAC** | Users can only access what their role allows — nothing more |
| **Hospital Data Isolation** | Hospital A can never see Hospital B's data |
| **Password Hashing** | Passwords stored as BCrypt hashes, never plain text |
| **JWT Token Security** | Login tokens expire, can be revoked, cryptographically signed |
| **Audit Trail** | Every data access and modification is logged |
| **Input Validation** | All inputs sanitized to prevent SQL injection and XSS |

---

## 🔄 Medical Record Transfer (Unique Feature)

When a patient switches hospitals:
1. New hospital **requests** the patient's records
2. Old hospital **reviews and approves** the request
3. **Patient must consent** to sharing their data
4. Only **specific records** are shared, not everything
5. Complete **audit trail** of who requested what and when

---

## 🛠️ Tech Stack

| Tool | What It Does |
|------|-------------|
| Java 17 | Programming language |
| Spring Boot 4.0 | Web framework |
| PostgreSQL | Database |
| Docker | Runs PostgreSQL |
| Maven | Build tool |
| Lombok | Reduces boilerplate code |
| Flyway | Database migrations |
| Spring Security | Authentication & authorization |
| JWT | Login tokens |
| Git/GitHub | Version control |

**GitHub**: [MohanVamsi08/healthcare-erp](https://github.com/MohanVamsi08/healthcare-erp)
