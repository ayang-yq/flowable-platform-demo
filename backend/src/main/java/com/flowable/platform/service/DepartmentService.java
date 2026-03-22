package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Department;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.DepartmentRepository;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository,
                            TenantRepository tenantRepository,
                            UserRepository userRepository,
                            AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return UUID.fromString(tenantIdStr);
    }

    public List<Department> getRootDepartments() {
        return departmentRepository.findByTenantIdAndParentIdIsNull(getTenantId());
    }

    public List<Department> getChildDepartments(UUID parentId) {
        return departmentRepository.findByParentId(parentId);
    }

    public Optional<Department> getDepartmentById(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .filter(dept -> dept.getTenant().getId().equals(getTenantId()));
    }

    public Department getDepartmentByIdOrThrow(UUID departmentId) {
        return getDepartmentById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found: " + departmentId));
    }

    public List<Department> getDescendants(UUID departmentId) {
        Department dept = getDepartmentByIdOrThrow(departmentId);
        return departmentRepository.findAllDescendants(getTenantId(), dept.getPath());
    }

    @Transactional
    public Department createDepartment(String name, String code, UUID parentId, UUID managerId) {
        UUID tenantId = getTenantId();

        if (departmentRepository.existsByTenantIdAndCode(tenantId, code)) {
            throw new IllegalArgumentException("Department code already exists: " + code);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        Department department = new Department(tenant, name, code);

        if (parentId != null) {
            Department parent = getDepartmentByIdOrThrow(parentId);
            department.setParent(parent);
            department.setLevel(parent.getLevel() + 1);
            department.setPath(parent.getPath() + code + "/");
        } else {
            department.setLevel(0);
            department.setPath("/" + code + "/");
        }

        if (managerId != null) {
            User manager = userRepository.findById(managerId)
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);
        auditService.logAction("DEPARTMENT_CREATED", "DEPARTMENT", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public Department updateDepartment(UUID departmentId, String name, UUID managerId) {
        Department department = getDepartmentByIdOrThrow(departmentId);
        UUID tenantId = getTenantId();

        if (name != null) {
            department.setName(name);
        }

        if (managerId != null) {
            User manager = userRepository.findById(managerId)
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .orElseThrow(() -> new IllegalArgumentException("Manager not found: " + managerId));
            department.setManager(manager);
        }

        Department saved = departmentRepository.save(department);
        auditService.logAction("DEPARTMENT_UPDATED", "DEPARTMENT", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public void addUsersToDepartment(UUID departmentId, Set<UUID> userIds) {
        Department department = getDepartmentByIdOrThrow(departmentId);
        UUID tenantId = getTenantId();

        for (UUID userId : userIds) {
            User user = userRepository.findById(userId)
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            user.getDepartments().add(department);
            userRepository.save(user);
        }
        auditService.logAction("DEPARTMENT_USERS_ADDED", "DEPARTMENT", departmentId.toString(), null);
    }

    @Transactional
    public void removeUsersFromDepartment(UUID departmentId, Set<UUID> userIds) {
        Department department = getDepartmentByIdOrThrow(departmentId);
        UUID tenantId = getTenantId();

        for (UUID userId : userIds) {
            User user = userRepository.findById(userId)
                    .filter(u -> u.getTenant().getId().equals(tenantId))
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            user.getDepartments().remove(department);
            userRepository.save(user);
        }
        auditService.logAction("DEPARTMENT_USERS_REMOVED", "DEPARTMENT", departmentId.toString(), null);
    }
}
