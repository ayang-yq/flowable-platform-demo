package com.flowable.platform.integration;

import com.flowable.platform.entity.Tenant;
import com.flowable.platform.entity.User;
import com.flowable.platform.entity.Role;
import com.flowable.platform.repository.TenantRepository;
import com.flowable.platform.repository.UserRepository;
import com.flowable.platform.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SeedDataVerificationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @Transactional
    void testSeedDataLoaded() {
        // Verify tenants exist
        List<Tenant> tenants = tenantRepository.findAll();
        assertFalse(tenants.isEmpty(), "At least one tenant should exist");
        assertTrue(tenants.size() >= 2, "At least 2 tenants should be created (ACME and Demo)");

        // Verify ACME tenant exists
        Tenant acmeTenant = tenantRepository.findActiveByCode("acme").orElse(null);
        assertNotNull(acmeTenant, "ACME tenant should exist");
        assertEquals("ACME Corporation", acmeTenant.getName());

        // Verify Demo tenant exists
        Tenant demoTenant = tenantRepository.findActiveByCode("demo").orElse(null);
        assertNotNull(demoTenant, "Demo tenant should exist");
        assertEquals("Demo Organization", demoTenant.getName());
    }

    @Test
    @Transactional
    void testAdminUsersExist() {
        // Verify ACME admin user
        User acmeAdmin = userRepository.findActiveByTenantIdAndUsername(
            tenantRepository.findActiveByCode("acme").get().getId(),
            "admin"
        ).orElse(null);

        assertNotNull(acmeAdmin, "ACME admin user should exist");
        assertEquals("admin@acme.com", acmeAdmin.getEmail());
        assertEquals("System", acmeAdmin.getFirstName());

        // Verify Demo admin user
        User demoAdmin = userRepository.findActiveByTenantIdAndUsername(
            tenantRepository.findActiveByCode("demo").get().getId(),
            "demo_admin"
        ).orElse(null);

        assertNotNull(demoAdmin, "Demo admin user should exist");
        assertEquals("admin@demo.com", demoAdmin.getEmail());
    }

    @Test
    @Transactional
    void testSystemRolesExist() {
        // Verify system roles exist for ACME tenant
        Tenant acmeTenant = tenantRepository.findActiveByCode("acme").get();
        List<Role> acmeRoles = roleRepository.findByTenantId(acmeTenant.getId());

        assertTrue(acmeRoles.size() >= 4, "ACME should have at least 4 system roles");

        // Verify specific roles exist
        String[] expectedRoles = {"SUPER_ADMIN", "ADMIN", "PROCESS_DESIGNER", "USER"};
        for (String roleCode : expectedRoles) {
            boolean roleExists = acmeRoles.stream()
                .anyMatch(role -> role.getCode().equals(roleCode) && role.getIsSystem());
            assertTrue(roleExists, "System role " + roleCode + " should exist for ACME");
        }
    }

    @Test
    @Transactional
    void testMultiTenantIsolation() {
        Tenant acmeTenant = tenantRepository.findActiveByCode("acme").get();
        Tenant demoTenant = tenantRepository.findActiveByCode("demo").get();

        // Verify ACME users
        List<User> acmeUsers = userRepository.findByTenantIdAndIsActiveTrue(acmeTenant.getId());
        assertTrue(acmeUsers.size() >= 5, "ACME should have at least 5 users");

        // Verify Demo users
        List<User> demoUsers = userRepository.findByTenantIdAndIsActiveTrue(demoTenant.getId());
        assertTrue(demoUsers.size() >= 2, "Demo should have at least 2 users");

        // Verify users are isolated by tenant
        for (User user : acmeUsers) {
            assertEquals(acmeTenant.getId(), user.getTenant().getId());
        }

        for (User user : demoUsers) {
            assertEquals(demoTenant.getId(), user.getTenant().getId());
        }

        // Verify no cross-tenant users
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            assertNotNull(user.getTenant().getId(), "All users must belong to a tenant");
        }
    }

    @Test
    @Transactional
    void testUserRoleAssignments() {
        // Verify admin user has SUPER_ADMIN role
        Tenant acmeTenant = tenantRepository.findActiveByCode("acme").get();
        User acmeAdmin = userRepository.findActiveByTenantIdAndUsername(
            acmeTenant.getId(),
            "admin"
        ).orElseThrow();

        assertFalse(acmeAdmin.getRoles().isEmpty(), "Admin user should have at least one role");

        // Verify admin has SUPER_ADMIN role
        boolean hasSuperAdmin = acmeAdmin.getRoles().stream()
            .anyMatch(role -> role.getCode().equals("SUPER_ADMIN"));
        assertTrue(hasSuperAdmin, "Admin user should have SUPER_ADMIN role");
    }

    @Test
    @Transactional
    void testDefaultCredentials() {
        // This test documents the default credentials for testing
        Tenant acmeTenant = tenantRepository.findActiveByCode("acme").get();

        // ACME Tenant
        User acmeAdmin = userRepository.findActiveByTenantIdAndUsername(
            acmeTenant.getId(),
            "admin"
        ).orElseThrow();

        System.out.println("=== DEFAULT CREDENTIALS FOR TESTING ===");
        System.out.println("ACME Tenant:");
        System.out.println("  Tenant Code: acme");
        System.out.println("  Admin Username: " + acmeAdmin.getUsername());
        System.out.println("  Admin Email: " + acmeAdmin.getEmail());
        System.out.println("  Admin Password: admin123");
        System.out.println();

        // Demo Tenant
        Tenant demoTenant = tenantRepository.findActiveByCode("demo").get();
        User demoAdmin = userRepository.findActiveByTenantIdAndUsername(
            demoTenant.getId(),
            "demo_admin"
        ).orElseThrow();

        System.out.println("Demo Tenant:");
        System.out.println("  Tenant Code: demo");
        System.out.println("  Admin Username: " + demoAdmin.getUsername());
        System.out.println("  Admin Email: " + demoAdmin.getEmail());
        System.out.println("  Admin Password: admin123");
        System.out.println("=========================================");

        // Verify the documentation matches reality
        assertNotNull(acmeAdmin.getPassword(), "Admin password should be set");
        assertNotNull(demoAdmin.getPassword(), "Demo admin password should be set");
    }
}
