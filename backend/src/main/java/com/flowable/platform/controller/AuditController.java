package com.flowable.platform.controller;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.dto.ApiResponse;
import com.flowable.platform.entity.AuditLog;
import com.flowable.platform.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> queryAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditLogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<Map<String, Object>> result = logs.getContent().stream()
                .map(log -> Map.<String, Object>of(
                        "id", log.getId().toString(),
                        "actionType", log.getActionType() != null ? log.getActionType() : "",
                        "entityType", log.getEntityType() != null ? log.getEntityType() : "",
                        "entityId", log.getEntityId() != null ? log.getEntityId() : "",
                        "username", log.getUsername() != null ? log.getUsername() : "",
                        "ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "",
                        "timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : "",
                        "details", log.getDetails() != null ? log.getDetails() : "{}"
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAuditLog(@PathVariable UUID id) {
        return auditLogRepository.findById(id)
                .map(log -> ResponseEntity.ok(ApiResponse.success(Map.<String, Object>of(
                        "id", log.getId().toString(),
                        "actionType", log.getActionType() != null ? log.getActionType() : "",
                        "entityType", log.getEntityType() != null ? log.getEntityType() : "",
                        "entityId", log.getEntityId() != null ? log.getEntityId() : "",
                        "username", log.getUsername() != null ? log.getUsername() : "",
                        "ipAddress", log.getIpAddress() != null ? log.getIpAddress() : "",
                        "timestamp", log.getTimestamp() != null ? log.getTimestamp().toString() : "",
                        "details", log.getDetails() != null ? log.getDetails() : "{}"
                ))))
                .orElse(ResponseEntity.notFound().build());
    }
}
