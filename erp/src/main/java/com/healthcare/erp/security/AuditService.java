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

    @Async
    public void log(String action, String entityType, String entityId,
                    UUID hospitalId, String ipAddress, String details) {
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

        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userEmail(email)
                .userRole(role)
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
