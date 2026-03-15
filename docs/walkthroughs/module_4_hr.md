# Module 4: HR Management — Walkthrough

> **Status:** ✅ Complete
> **What it does:** Non-doctor staff management, shift scheduling, and leave request workflows.

---

## What Was Built

### 3 New Entities

| Entity | Table | What It Is |
|--------|-------|-----------|
| **Staff** | `staff` | Non-doctor hospital employees (Nurses, Receptionists, Techs). Linked to Department and optional User account. |
| **Shift** | `shifts` | Working schedules assigned to Staff (Date, Start Time, End Time, Type). |
| **LeaveRequest**| `leave_requests`| SICK, CASUAL, etc. leaves submitted by Staff, needing Admin approval. |

### Leave Status Flow

```
PENDING → APPROVED
        → REJECTED
        → CANCELLED
```

---

## New API Endpoints

### Staff
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/staff` | ADMIN |
| GET | `/api/hospitals/{id}/staff` | ADMIN |
| GET | `/api/hospitals/{id}/staff/{staffId}` | ADMIN |
| PUT | `/api/hospitals/{id}/staff/{staffId}` | ADMIN |
| DELETE | `/api/hospitals/{id}/staff/{staffId}` | ADMIN |

### Shifts
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/shifts` | ADMIN |
| GET | `/api/hospitals/{id}/shifts` (query by date) | ADMIN |
| GET | `/api/hospitals/{id}/shifts/staff/{staffId}` (query by date range) | ADMIN, NURSE, RECEPTIONIST |
| PUT | `/api/hospitals/{id}/shifts/{shiftId}` | ADMIN |

### Leave Requests
| Method | URL | Access |
|--------|-----|--------|
| POST | `/api/hospitals/{id}/leave-requests` | Any authenticated |
| GET | `/api/hospitals/{id}/leave-requests` | ADMIN |
| GET | `/api/hospitals/{id}/leave-requests/pending` | ADMIN |
| PATCH | `/api/hospitals/{id}/leave-requests/{leaveId}/approve` | ADMIN |
| PATCH | `/api/hospitals/{id}/leave-requests/{leaveId}/reject` | ADMIN |

---

## Files Created (20 new)

```
model/Staff.java, Shift.java, LeaveRequest.java
model/LeaveType.java, LeaveStatus.java, ShiftType.java
repository/StaffRepository.java, ShiftRepository.java, LeaveRequestRepository.java
service/StaffService.java, ShiftService.java, LeaveRequestService.java
controller/StaffController.java, ShiftController.java, LeaveRequestController.java
dto/StaffDTO.java, ShiftDTO.java, LeaveRequestDTO.java
db/migration/V9, V10 SQL files
```

## Test Results

| # | Test | Result |
|---|------|--------|
| 1 | Create Staff (Nurse, EMP-001) | ✅ |
| 2 | List Hospital Staff | ✅ 1 found |
| 3 | Schedule Shift (Morning 8am-4pm) | ✅ |
| 4 | Submit Leave Request (Sick, 3 days) | ✅ |
| 5 | View Pending Leaves | ✅ PENDING |
| 6 | Approve Leave | ✅ APPROVED |
