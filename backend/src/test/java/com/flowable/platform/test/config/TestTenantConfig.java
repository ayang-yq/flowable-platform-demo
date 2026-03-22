package com.flowable.platform.test.config;

import java.util.UUID;

/**
 * Test tenant configuration with constants for multi-tenant testing.
 */
public class TestTenantConfig {

    public static final UUID TEST_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String TEST_TENANT_CODE = "test-tenant";
    public static final String TEST_TENANT_NAME = "Test Tenant";

    private TestTenantConfig() {
        // Utility class
    }
}
