package com.flowable.platform.service;

import com.flowable.platform.entity.AuditLog;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logAction(String actionType, String entityType, String entityId, Object details) {
        try {
            AuditLog auditLog = new AuditLog();

            // TODO: Get current user and tenant from security context
            // auditLog.setUserId(getCurrentUserId());
            // auditLog.setTenantId(getCurrentTenantId());

            auditLog.setActionType(actionType);
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setIpAddress(getClientIpAddress());
            auditLog.setTimestamp(LocalDateTime.now());

            // Store details as JSON, or empty object if null
            if (details != null) {
                // For now, store as string representation
                // In production, use ObjectMapper to convert to JSON
                auditLog.setDetails("{}");
            } else {
                auditLog.setDetails("{}");
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't throw exceptions in async audit logging to avoid breaking main flow
            // Log the error instead
            System.err.println("Failed to log audit action: " + e.getMessage());
        }
    }

    private String getClientIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return null;
    }
}
