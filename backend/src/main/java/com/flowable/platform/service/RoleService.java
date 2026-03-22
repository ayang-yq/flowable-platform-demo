package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Role;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.repository.RoleRepository;
import com.flowable.platform.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final AuditService auditService;

    public RoleService(RoleRepository roleRepository, TenantRepository tenantRepository,
                      AuditService auditService) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.auditService = auditService;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return UUID.fromString(tenantIdStr);
    }

    public List<Role> listRoles() {
        return roleRepository.findByTenantId(getTenantId());
    }

    public Optional<Role> getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .filter(role -> role.getTenant().getId().equals(getTenantId()));
    }

    public Role getRoleByIdOrThrow(UUID roleId) {
        return getRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
    }

    @Transactional
    public Role createRole(String name, String code, String description, String permissions) {
        UUID tenantId = getTenantId();

        if (roleRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new IllegalArgumentException("Role code already exists: " + code);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Role role = new Role(tenant, name, code);
        role.setDescription(description);
        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        auditService.logAction("ROLE_CREATED", "ROLE", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public Role updateRole(UUID roleId, String name, String description, String permissions) {
        Role role = getRoleByIdOrThrow(roleId);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException("Cannot modify system role: " + role.getCode());
        }

        if (name != null) role.setName(name);
        if (description != null) role.setDescription(description);
        if (permissions != null) role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        auditService.logAction("ROLE_UPDATED", "ROLE", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = getRoleByIdOrThrow(roleId);

        if (role.getIsSystem()) {
            throw new IllegalArgumentException("Cannot delete system role: " + role.getCode());
        }

        if (!role.getUsers().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete role with assigned users");
        }

        roleRepository.delete(role);
        auditService.logAction("ROLE_DELETED", "ROLE", roleId.toString(), null);
    }
}
