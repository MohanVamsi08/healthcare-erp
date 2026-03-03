# Module 2: Admin & Security — Walkthrough

> **Status:** ✅ Complete
> **What it does:** User accounts, JWT authentication, and role-based access control (RBAC)

---

## What Was Built

### Role Hierarchy

```
SUPER_ADMIN (you, the ERP owner)
  └── Creates HOSPITAL_ADMIN for each hospital
        └── Creates staff: DOCTOR, NURSE, RECEPTIONIST, BILLING_STAFF, PHARMACIST
```

No open registration — only admins create accounts.

### How Login Works

```
1. POST /api/auth/login { email, password }
2. Server checks password (BCrypt hash)
3. Returns JWT token with: userId, email, role, hospitalId
4. All future requests include: Authorization: Bearer <token>
5. Server validates token on every request
```

### Default Account (Seeded on Startup)
- **Email:** admin@healthcare-erp.com
- **Password:** admin123
- **Role:** SUPER_ADMIN

---

## New API Endpoints

### Authentication
| Method | URL | Access | What |
|--------|-----|--------|------|
| POST | `/api/auth/login` | Public | Login, get JWT token |

### User Management
| Method | URL | Access | What |
|--------|-----|--------|------|
| POST | `/api/admin/users` | SUPER_ADMIN | Create hospital admin |
| POST | `/api/hospitals/{id}/users` | HOSPITAL_ADMIN+ | Create staff user |
| GET | `/api/hospitals/{id}/users` | HOSPITAL_ADMIN+ | List hospital staff |
| GET | `/api/hospitals/{id}/users/{userId}` | HOSPITAL_ADMIN+ | Get specific user |
| PUT | `/api/hospitals/{id}/users/{userId}` | HOSPITAL_ADMIN+ | Update user |
| DELETE | `/api/hospitals/{id}/users/{userId}` | HOSPITAL_ADMIN+ | Deactivate user |

### Existing Endpoints (Now Protected)
All previous endpoints (`/api/hospitals`, `/api/hospitals/{id}/departments`, etc.) now require a JWT token.

---

## Files Created

```
NEW:
├── model/Role.java                        ← 7 roles enum
├── model/User.java                        ← User entity
├── repository/UserRepository.java         ← DB queries for users
├── security/
│   ├── JwtService.java                    ← Token creation & validation
│   ├── JwtAuthenticationFilter.java       ← Checks every request for JWT
│   ├── CustomUserDetailsService.java      ← Loads user from DB
│   └── SecurityConfig.java               ← Endpoint rules & RBAC
├── service/AuthService.java               ← Login logic
├── service/UserService.java               ← User CRUD with role checks
├── controller/AuthController.java         ← Login endpoint
├── controller/UserController.java         ← User management endpoints
├── config/DataSeeder.java                 ← Creates default SUPER_ADMIN
├── dto/LoginRequest.java
├── dto/AuthResponse.java
├── dto/CreateUserRequest.java
├── dto/UserDTO.java

MODIFIED:
├── pom.xml                                ← Added security + JWT dependencies
├── application.properties                 ← JWT secret + expiration
├── dto/HospitalDTO.java                   ← Fixed Boolean isActive

MIGRATION:
├── V4__create_users_table.sql             ← Users table
```

---

## Test Results

| Test | Result |
|------|--------|
| Login as SUPER_ADMIN | ✅ Returns JWT token |
| Create hospital with token | ✅ Hospital created |
| SUPER_ADMIN creates HOSPITAL_ADMIN | ✅ User created with correct role |
| HOSPITAL_ADMIN logs in | ✅ Returns JWT with hospitalId |
| HOSPITAL_ADMIN creates Doctor | ✅ Doctor created under their hospital |
| Access without token | ✅ Blocked (403 Forbidden) |

---

## Key Concepts Used

| Concept | What It Does |
|---------|-------------|
| **BCrypt** | Hashes passwords so they're never stored as plain text |
| **JWT** | JSON Web Token — a signed token containing user info |
| **@PreAuthorize** | Annotation that restricts endpoints by role |
| **OncePerRequestFilter** | Filter that runs on every HTTP request to check JWT |
| **CommandLineRunner** | Runs code on app startup (seeds SUPER_ADMIN) |
| **SecurityFilterChain** | Configures which endpoints are public vs protected |
