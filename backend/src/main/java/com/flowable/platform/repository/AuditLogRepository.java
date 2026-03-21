package com.flowable.platform.repository;

import com.flowable.platform.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByTenantIdOrderByTimestampDesc(UUID tenantId);

    List<AuditLog> findByTenantIdAndUserIdOrderByTimestampDesc(UUID tenantId, UUID userId);

    List<AuditLog> findByTenantIdAndActionTypeOrderByTimestampDesc(UUID tenantId, String actionType);

    List<AuditLog> findByTenantIdAndTimestampBetweenOrderByTimestampDesc(
            UUID tenantId, LocalDateTime startDate, LocalDateTime endDate);
}
