package com.healthcare.erp.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Main audit facade. Captures identity synchronously on the calling thread,
 * then delegates to AuditLogWriter (a separate bean) for true @Async persistence.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogWriter auditLogWriter;

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

        // Delegate to separate bean — @Async proxy works correctly here
        auditLogWriter.write(action, entityType, entityId, hospitalId, ipAddress, details, email, role);
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

    public void logExport(String entityType, String format, UUID hospitalId, String ip) {
        log("EXPORT", entityType, null, hospitalId, ip, "Format: " + format);
    }
}
