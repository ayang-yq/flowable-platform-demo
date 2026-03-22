package com.flowable.platform.test.util;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Manages tenant context for tests using ThreadLocal storage.
 * Provides utilities for setting, getting, and clearing tenant context.
 */
public class TestTenantContext {

    private final UUID defaultTenantId;
    private final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public TestTenantContext(UUID defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    /**
     * Set test tenant context for current thread
     */
    public void setTestTenant() {
        currentTenant.set(defaultTenantId);
    }

    /**
     * Set specific tenant context for current thread
     */
    public void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Get current tenant ID from context
     */
    public UUID getTenantId() {
        return currentTenant.get();
    }

    /**
     * Clear tenant context
     */
    public void clear() {
        currentTenant.remove();
    }

    /**
     * Execute callable with tenant context
     */
    public <T> T withTenantId(UUID tenantId, Callable<T> callable) throws Exception {
        try {
            setTenantId(tenantId);
            return callable.call();
        } finally {
            clear();
        }
    }

    /**
     * Check if tenant context is set
     */
    public boolean isSet() {
        return currentTenant.get() != null;
    }

    /**
     * Get default tenant ID
     */
    public UUID getDefaultTenantId() {
        return defaultTenantId;
    }
}
