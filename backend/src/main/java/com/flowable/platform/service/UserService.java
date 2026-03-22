package com.flowable.platform.service;

import com.flowable.platform.config.MultiTenantFilter;
import com.flowable.platform.entity.Department;
import com.flowable.platform.entity.Role;
import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.repository.DepartmentRepository;
import com.flowable.platform.repository.RoleRepository;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       DepartmentRepository departmentRepository, TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    private UUID getTenantId() {
        String tenantIdStr = MultiTenantFilter.getCurrentTenantId();
        if (tenantIdStr == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return UUID.fromString(tenantIdStr);
    }

    public List<User> listUsers() {
        return userRepository.findByTenantIdAndIsActiveTrue(getTenantId());
    }

    public Page<User> listUsers(Pageable pageable) {
        // For paginated queries, use custom query or spec
        return userRepository.findAll(pageable); // TODO: filter by tenant
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getTenant().getId().equals(getTenantId()));
    }

    public User getUserByIdOrThrow(UUID userId) {
        return getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    @Transactional
    public User createUser(String username, String email, String password,
                          String firstName, String lastName, String phone,
                          Set<String> roleCodes, Set<String> departmentCodes) {
        UUID tenantId = getTenantId();

        if (userRepository.existsByTenantIdAndUsername(tenantId, username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        User user = new User(tenant, username, email, passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        if (firstName != null && lastName != null) {
            user.setDisplayName(firstName + " " + lastName);
        }

        if (roleCodes != null && !roleCodes.isEmpty()) {
            Set<Role> roles = roleCodes.stream()
                    .map(code -> roleRepository.findByTenantIdAndCode(tenantId, code)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + code)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (departmentCodes != null && !departmentCodes.isEmpty()) {
            Set<Department> departments = departmentCodes.stream()
                    .map(code -> departmentRepository.findByTenantIdAndCode(tenantId, code)
                            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + code)))
                    .collect(Collectors.toSet());
            user.setDepartments(departments);
        }

        User saved = userRepository.save(user);
        auditService.logAction("USER_CREATED", "USER", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public User updateUser(UUID userId, String email, String firstName, String lastName,
                          String phone, String displayName, String locale, String timezone,
                          Set<String> roleCodes, Set<String> departmentCodes) {
        User user = getUserByIdOrThrow(userId);
        UUID tenantId = getTenantId();

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByTenantIdAndEmail(tenantId, email)) {
                throw new IllegalArgumentException("Email already exists: " + email);
            }
            user.setEmail(email);
        }

        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phone != null) user.setPhone(phone);
        if (displayName != null) user.setDisplayName(displayName);
        if (locale != null) user.setLocale(locale);
        if (timezone != null) user.setTimezone(timezone);

        if (roleCodes != null) {
            Set<Role> roles = roleCodes.stream()
                    .map(code -> roleRepository.findByTenantIdAndCode(tenantId, code)
                            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + code)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        if (departmentCodes != null) {
            Set<Department> departments = departmentCodes.stream()
                    .map(code -> departmentRepository.findByTenantIdAndCode(tenantId, code)
                            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + code)))
                    .collect(Collectors.toSet());
            user.setDepartments(departments);
        }

        User saved = userRepository.save(user);
        auditService.logAction("USER_UPDATED", "USER", saved.getId().toString(), null);
        return saved;
    }

    @Transactional
    public void deactivateUser(UUID userId) {
        User user = getUserByIdOrThrow(userId);
        user.setIsActive(false);
        userRepository.save(user);
        auditService.logAction("USER_DEACTIVATED", "USER", userId.toString(), null);
    }
}
