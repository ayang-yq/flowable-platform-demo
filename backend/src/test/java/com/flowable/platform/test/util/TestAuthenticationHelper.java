package com.flowable.platform.test.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.preemptive;

/**
 * Helper for creating authenticated REST Assured request specifications.
 */
public class TestAuthenticationHelper {

    public static final String TEST_ADMIN_USERNAME = "test-admin";
    public static final String TEST_ADMIN_PASSWORD = "admin123";
    public static final String TEST_USER_USERNAME = "test-user";
    public static final String TEST_USER_PASSWORD = "user123";
    public static final String TEST_GUEST_USERNAME = "test-guest";
    public static final String TEST_GUEST_PASSWORD = "guest123";
    public static final String TEST_TENANT_CODE = "test-tenant";

    /**
     * Create request specification with basic auth
     */
    public static RequestSpecification withBasicAuth(String username, String password) {
        return new RequestSpecBuilder()
            .setContentType("application/json")
            .addHeader("X-Tenant-Code", TEST_TENANT_CODE)
            .setAuth(preemptive().basic(username, password))
            .build();
    }

    /**
     * Create request specification with test admin credentials
     */
    public static RequestSpecification asAdmin() {
        return withBasicAuth(TEST_ADMIN_USERNAME, TEST_ADMIN_PASSWORD);
    }

    /**
     * Create request specification with test user credentials
     */
    public static RequestSpecification asUser() {
        return withBasicAuth(TEST_USER_USERNAME, TEST_USER_PASSWORD);
    }

    /**
     * Create request specification with test guest credentials
     */
    public static RequestSpecification asGuest() {
        return withBasicAuth(TEST_GUEST_USERNAME, TEST_GUEST_PASSWORD);
    }

    /**
     * Create request specification with JWT token
     */
    public static RequestSpecification withToken(String token) {
        return new RequestSpecBuilder()
            .setContentType("application/json")
            .addHeader("X-Tenant-Code", TEST_TENANT_CODE)
            .addHeader("Authorization", "Bearer " + token)
            .build();
    }
}
