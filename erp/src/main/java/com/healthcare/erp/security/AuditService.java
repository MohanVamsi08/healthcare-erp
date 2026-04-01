package com.healthcare.erp.security;

import com.healthcare.erp.model.AuditLog;
import com.healthcare.erp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Captures the caller's identity from the SecurityContext on the calling thread,
     * then delegates the actual DB write to an async method so identity is never lost.
     */
    public void log(String action, String entityType, String entityId,
                    UUID hospitalId, String ipAddress, String details) {
        // Capture identity on the CALLING thread (where SecurityContext is valid)
        String email = "anonymous";
        String role = "NONE";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            email = auth.getName();
            role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("UNKNOWN");
        }

        // Pass captured identity to async writer
        writeAuditLog(action, entityType, entityId, hospitalId, ipAddress, details, email, role);
    }

    @Async
    protected void writeAuditLog(String action, String entityType, String entityId,
                                 UUID hospitalId, String ipAddress, String details,
                                 String userEmail, String userRole) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userEmail(userEmail)
                .userRole(userRole)
                .hospitalId(hospitalId)
                .ipAddress(ipAddress)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    // Convenience methods
    public void logCreate(String entityType, String entityId, UUID hospitalId, String ip) {
        log("CREATE", entityType, entityId, hospitalId, ip, null);
    }

    public void logRead(String entityType, String entityId, UUID hospitalId, String ip) {
        log("READ", entityType, entityId, hospitalId, ip, null);
    }

    public void logUpdate(String entityType, String entityId, UUID hospitalId, String ip) {
        log("UPDATE", entityType, entityId, hospitalId, ip, null);
    }

    public void logDelete(String entityType, String entityId, UUID hospitalId, String ip) {
        log("DELETE", entityType, entityId, hospitalId, ip, null);
    }

    public void logLogin(String email, String ip, boolean success) {
        log(success ? "LOGIN_SUCCESS" : "LOGIN_FAILED", "AUTH", null, null, ip,
                success ? null : "Failed login attempt for: " + email);
    }
}
