package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Dashboard;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.DashboardRepository;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public DashboardService(DashboardRepository dashboardRepository, TenantRepository tenantRepository,
                           UserRepository userRepository) {
        this.dashboardRepository = dashboardRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) throw new IllegalStateException("No tenant context available");
        return UUID.fromString(tenantIdStr);
    }

    public List<Dashboard> getUserDashboards(String username) {
        UUID tenantId = getTenantId();
        User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return dashboardRepository.findByTenantIdAndOwnerId(tenantId, user.getId());
    }

    public List<Dashboard> getPublicDashboards() {
        return dashboardRepository.findByTenantIdAndIsPublicTrue(getTenantId());
    }

    public Optional<Dashboard> getDashboardById(UUID id) {
        return dashboardRepository.findById(id)
                .filter(d -> d.getTenant().getId().equals(getTenantId()));
    }

    @Transactional
    public Dashboard createDashboard(String username, String name, String description, String layoutJson) {
        UUID tenantId = getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        User owner = userRepository.findByTenantIdAndUsername(tenantId, username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Dashboard dashboard = new Dashboard();
        dashboard.setTenant(tenant);
        dashboard.setOwner(owner);
        dashboard.setName(name);
        dashboard.setDescription(description);
        dashboard.setLayoutJson(layoutJson != null ? layoutJson : "{}");
        return dashboardRepository.save(dashboard);
    }

    @Transactional
    public Dashboard updateDashboard(UUID id, String name, String layoutJson) {
        Dashboard dashboard = getDashboardById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dashboard not found"));
        if (name != null) dashboard.setName(name);
        if (layoutJson != null) dashboard.setLayoutJson(layoutJson);
        return dashboardRepository.save(dashboard);
    }
}
