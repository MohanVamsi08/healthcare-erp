package com.healthcare.erp.security;

import com.healthcare.erp.model.AuditLog;
import com.healthcare.erp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Separate bean for async audit log persistence.
 * This ensures @Async goes through Spring's proxy — unlike self-invocation within AuditService.
 */
@Component
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void write(String action, String entityType, String entityId,
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
}
