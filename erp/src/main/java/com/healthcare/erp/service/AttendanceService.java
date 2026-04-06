package com.healthcare.erp.service;

import com.healthcare.erp.dto.AttendanceDTO;
import com.healthcare.erp.exception.ResourceNotFoundException;
import com.healthcare.erp.model.*;
import com.healthcare.erp.repository.*;
import com.healthcare.erp.security.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    @org.springframework.beans.factory.annotation.Value("${attendance.half-day-threshold-hours:4}")
    private int halfDayThresholdHours;

    private final AttendanceRepository attendanceRepository;
    private final StaffRepository staffRepository;
    private final HospitalRepository hospitalRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getByHospitalAndDate(UUID hospitalId, LocalDate date) {
        if (!hospitalRepository.existsById(hospitalId))
            throw new ResourceNotFoundException("Hospital", hospitalId);
        auditService.logRead("Attendance", "LIST:date=" + date, hospitalId, null);
        return attendanceRepository.findByHospitalIdAndDate(hospitalId, date).stream()
                .map(AttendanceDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceDTO> getByStaff(UUID hospitalId, UUID staffId, LocalDate start, LocalDate end) {
        // Tenant check: ensure staff belongs to this hospital
        getStaffWithCheck(hospitalId, staffId);
        auditService.logRead("Attendance", "LIST:staff=" + staffId, hospitalId, null);
        return attendanceRepository.findByStaffIdAndHospitalIdAndDateBetween(staffId, hospitalId, start, end).stream()
                .map(AttendanceDTO::fromEntity).toList();
    }

    /**
     * Record clock-in for a staff member.
     */
    public AttendanceDTO clockIn(UUID hospitalId, UUID staffId) {
        Staff staff = getStaffWithCheck(hospitalId, staffId);
        enforceOrSelfOnly(hospitalId, staffId);
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByStaffIdAndDate(staffId, today))
            throw new IllegalArgumentException("Attendance already recorded for today");

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Attendance attendance = Attendance.builder()
                .staff(staff)
                .hospital(hospital)
                .date(today)
                .clockIn(LocalTime.now())
                .status(AttendanceStatus.PRESENT)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        auditService.log("CLOCK_IN", "Attendance", saved.getId().toString(), hospitalId, null,
                "Staff " + staffId + " clocked in");
        return AttendanceDTO.fromEntity(saved);
    }

    /**
     * Record clock-out and compute hours worked.
     */
    public AttendanceDTO clockOut(UUID hospitalId, UUID staffId) {
        getStaffWithCheck(hospitalId, staffId);
        enforceOrSelfOnly(hospitalId, staffId);
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByStaffIdAndDate(staffId, today)
                .orElseThrow(() -> new IllegalArgumentException("No clock-in found for today"));

        if (attendance.getClockOut() != null)
            throw new IllegalArgumentException("Already clocked out");

        attendance.setClockOut(LocalTime.now());

        // Compute hours worked
        if (attendance.getClockIn() != null) {
            long minutes = Duration.between(attendance.getClockIn(), attendance.getClockOut()).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            attendance.setHoursWorked(hours);

            // Auto-set HALF_DAY if less than 4 hours
            if (hours.compareTo(BigDecimal.valueOf(halfDayThresholdHours)) < 0) {
                attendance.setStatus(AttendanceStatus.HALF_DAY);
            }
        }

        Attendance saved = attendanceRepository.save(attendance);
        auditService.log("CLOCK_OUT", "Attendance", saved.getId().toString(), hospitalId, null,
                "Staff " + staffId + " clocked out, hours: " + saved.getHoursWorked());
        return AttendanceDTO.fromEntity(saved);
    }

    /**
     * Mark absent or on-leave (admin action).
     */
    public AttendanceDTO markStatus(UUID hospitalId, UUID staffId, LocalDate date, AttendanceStatus status) {
        Staff staff = getStaffWithCheck(hospitalId, staffId);
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        Attendance attendance = attendanceRepository.findByStaffIdAndDate(staffId, date)
                .orElseGet(() -> Attendance.builder()
                        .staff(staff).hospital(hospital).date(date).build());

        attendance.setStatus(status);
        Attendance saved = attendanceRepository.save(attendance);
        auditService.log("MARK_STATUS", "Attendance", saved.getId().toString(), hospitalId, null,
                "Staff " + staffId + " marked " + status + " on " + date);
        return AttendanceDTO.fromEntity(saved);
    }

    private Staff getStaffWithCheck(UUID hospitalId, UUID staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", staffId));
        if (!staff.getHospital().getId().equals(hospitalId))
            throw new ResourceNotFoundException("Staff", staffId);
        return staff;
    }

    /**
     * For non-admin roles (NURSE, RECEPTIONIST), enforce that the authenticated user
     * can only clock in/out for themselves.
     */
    private void enforceOrSelfOnly(UUID hospitalId, UUID staffId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_HOSPITAL_ADMIN"));
        if (isAdmin) return;

        // Non-admin: the authenticated user's linked staff record must match
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        Staff linkedStaff = staffRepository.findByUserId(user.getId()).orElse(null);
        if (linkedStaff == null || !linkedStaff.getId().equals(staffId)) {
            throw new IllegalArgumentException("You can only clock in/out for yourself");
        }
    }
}
