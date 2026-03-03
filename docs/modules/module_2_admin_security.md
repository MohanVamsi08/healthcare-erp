# Module 2: Admin & Security — Implementation Plan

> **Status:** 🔲 In Progress
> **What it does:** User accounts, JWT login, role-based access control (RBAC)

---

## How the Role Hierarchy Works

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

> **There is NO open registration.** Only admins can create accounts.

---

## What We're Building

| # | What | Why |
|---|------|-----|
| 1 | Spring Security + JWT dependencies | Authentication framework |
| 2 | `users` table (V4 migration) | Store user accounts |
| 3 | Role enum + User entity | Define roles and user model |
| 4 | JWT token service | Generate/validate login tokens |
| 5 | Security filter | Check every request for valid token |
| 6 | Login endpoint | `POST /api/auth/login` |
| 7 | User management endpoints | Admins create/manage users |
| 8 | Default SUPER_ADMIN seed | First account on startup |

---

## New API Endpoints

### Authentication
| Method | URL | Who | What |
|--------|-----|-----|------|
| POST | `/api/auth/login` | Anyone | Login → get JWT token |

### User Management
| Method | URL | Who | What |
|--------|-----|-----|------|
| POST | `/api/admin/users` | SUPER_ADMIN | Create hospital admin |
| POST | `/api/hospitals/{id}/users` | HOSPITAL_ADMIN | Create staff user |
| GET | `/api/hospitals/{id}/users` | HOSPITAL_ADMIN | List hospital staff |
| PUT | `/api/hospitals/{id}/users/{userId}` | HOSPITAL_ADMIN | Update staff |
| DELETE | `/api/hospitals/{id}/users/{userId}` | HOSPITAL_ADMIN | Deactivate staff |

### Permission Matrix (RBAC)
| Action | SUPER_ADMIN | HOSPITAL_ADMIN | DOCTOR | NURSE | RECEPTIONIST | BILLING |
|--------|------------|----------------|--------|-------|-------------|---------|
| Create hospitals | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Create hospital admin | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Create staff users | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| View patients | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Create patients | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| View medical records | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ |
| Generate bills | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |

---

## Files To Create

```
NEW FILES:
├── model/Role.java                          (enum: SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR, etc.)
├── model/User.java                          (user entity)
├── repository/UserRepository.java
├── security/JwtService.java                 (token generation/validation)
├── security/JwtAuthenticationFilter.java    (checks every request)
├── security/CustomUserDetailsService.java   (loads user from DB)
├── security/SecurityConfig.java             (endpoint rules)
├── service/AuthService.java                 (login logic)
├── service/UserService.java                 (user CRUD)
├── controller/AuthController.java           (login endpoint)
├── controller/UserController.java           (user management)
├── dto/LoginRequest.java
├── dto/AuthResponse.java
├── dto/CreateUserRequest.java
├── dto/UserDTO.java

MODIFIED:
├── pom.xml                    (add security + JWT dependencies)
├── application.properties     (JWT secret + expiration)

MIGRATION:
├── V4__create_users_table.sql
```

---

## Seed Data

On first startup, a default SUPER_ADMIN is created:
- **Email:** admin@healthcare-erp.com
- **Password:** admin123
- **Role:** SUPER_ADMIN

---

## How Login Works (JWT Flow)

```
1. User sends: POST /api/auth/login { email, password }
2. Server checks password against BCrypt hash in database
3. If valid → Server creates a JWT token containing: userId, email, role, hospitalId
4. Server sends back: { token: "eyJhbG...", email, role, hospitalId }
5. For all future requests, user includes: Authorization: Bearer eyJhbG...
6. Server validates token on every request → allows or denies access
```
