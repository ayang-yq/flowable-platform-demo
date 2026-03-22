package com.flowable.platform.repository;

import com.flowable.platform.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    List<Dashboard> findByTenantIdAndOwnerId(UUID tenantId, UUID ownerId);

    List<Dashboard> findByTenantIdAndIsPublicTrue(UUID tenantId);
}
