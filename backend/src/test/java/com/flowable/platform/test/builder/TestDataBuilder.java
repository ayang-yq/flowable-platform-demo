package com.flowable.platform.test.builder;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.entity.Role;
import com.flowable.platform.test.config.TestTenantConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Builder for creating test data entities with fluent API.
 */
public class TestDataBuilder {

    private UUID userId;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private UUID tenantId;
    private String tenantCode;
    private String tenantName;
    private Set<Role> roles;
    private Boolean isActive;
    private String locale;
    private String timezone;

    private TestDataBuilder() {
        this.roles = new HashSet<>();
        this.isActive = true;
        this.locale = "en";
        this.timezone = "UTC";
    }

    // User builders
    public static TestDataBuilder aUser() {
        return new TestDataBuilder()
            .withUserId(UUID.randomUUID())
            .withUsername("test-user")
            .withEmail("test@example.com")
            .withPassword("$2a$10$test.password.hash")
            .withTenantId(TestTenantConfig.TEST_TENANT_ID)
            .withTenantCode(TestTenantConfig.TEST_TENANT_CODE)
            .withTenantName(TestTenantConfig.TEST_TENANT_NAME);
    }

    public static TestDataBuilder anAdmin() {
        return aUser()
            .withUsername("test-admin")
            .withEmail("admin@example.com")
            .withRole("ADMIN");
    }

    public static TestDataBuilder aGuest() {
        return aUser()
            .withUsername("test-guest")
            .withEmail("guest@example.com")
            .withRole("GUEST");
    }

    public static TestDataBuilder anInactiveUser() {
        return aUser()
            .withUsername("test-inactive")
            .withEmail("inactive@example.com")
            .withIsActive(false);
    }

    // With methods
    public TestDataBuilder withUserId(UUID userId) {
        this.userId = userId;
        return this;
    }

    public TestDataBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public TestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TestDataBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    public TestDataBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public TestDataBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public TestDataBuilder withTenantId(UUID tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public TestDataBuilder withTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
        return this;
    }

    public TestDataBuilder withTenantName(String tenantName) {
        this.tenantName = tenantName;
        return this;
    }

    public TestDataBuilder withRole(String roleName) {
        Role role = new Role();
        role.setName(roleName);
        role.setDescription(roleName + " role");
        this.roles.add(role);
        return this;
    }

    public TestDataBuilder withRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public TestDataBuilder withIsActive(Boolean isActive) {
        this.isActive = isActive;
        return this;
    }

    public TestDataBuilder withLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public TestDataBuilder withTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    // Build methods
    public User buildUser() {
        Tenant tenant = new Tenant(tenantName, tenantCode);
        tenant.setId(tenantId);
        
        User user = new User(tenant, username, email, password);
        user.setId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setIsActive(isActive);
        user.setLocale(locale);
        user.setTimezone(timezone);
        user.setRoles(roles);
        return user;
    }

    public Tenant buildTenant() {
        Tenant tenant = new Tenant(tenantName, tenantCode);
        tenant.setId(tenantId);
        tenant.setIsActive(true);
        return tenant;
    }

    public Role buildRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(name + " role");
        return role;
    }
}
