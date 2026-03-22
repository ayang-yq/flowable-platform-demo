package com.flowable.platform.test.integration;

import com.flowable.platform.service.JwtTokenService;
import com.flowable.platform.test.config.TestTenantConfig;
import com.flowable.platform.test.util.TestTenantContext;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * Base class for integration tests using REST Assured and Testcontainers.
 * 
 * <p>Provides:
 * <ul>
 *   <li>Testcontainers PostgreSQL for database testing</li>
 *   <li>REST Assured configuration for HTTP testing</li>
 *   <li>Tenant context management</li>
 *   <li>Authentication helpers</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>{@code
 * class MyApiTest extends AbstractIntegrationTest {
 *     
 *     @Test
 *     void shouldGetData() {
 *         givenWithAuth(adminToken)
 *             .get("/api/data")
 *         .then()
 *             .statusCode(200);
 *     }
 * }
 * }</pre>
 */
@DisplayName("Abstract Integration Test Base")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("flowable_platform")
            .withUsername("flowable")
            .withPassword("flowable")
            .withReuse(true);

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    @Autowired
    protected JwtTokenService jwtTokenService;

    protected static TestTenantContext testTenantContext;
    protected String adminToken;
    protected String userToken;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeAll
    static void initTestTenantContext() {
        testTenantContext = new TestTenantContext(TestTenantConfig.TEST_TENANT_ID);
    }

    @BeforeEach
    void setUp() {
        RestAssured.config = RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        RestAssured.port = port;
        RestAssured.basePath = "/api";

        // Set test tenant
        testTenantContext.setTestTenant();

        // Generate test JWT tokens
        String tenantId = TestTenantConfig.TEST_TENANT_ID.toString();
        adminToken = jwtTokenService.generateToken("test-admin", tenantId);
        userToken = jwtTokenService.generateToken("test-user", tenantId);
    }

    /**
     * Get authenticated request specification with Bearer token
     */
    protected io.restassured.specification.RequestSpecification givenWithAuth(String token) {
        return io.restassured.RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }

    /**
     * Get authenticated request specification as admin
     */
    protected io.restassured.specification.RequestSpecification givenAsAdmin() {
        return givenWithAuth(adminToken);
    }

    /**
     * Get authenticated request specification as regular user
     */
    protected io.restassured.specification.RequestSpecification givenAsUser() {
        return givenWithAuth(userToken);
    }

    /**
     * Set tenant context for specific tenant
     */
    protected void setTenantContext(UUID tenantId) {
        testTenantContext.setTenantId(tenantId);
    }

    /**
     * Clear tenant context
     */
    protected void clearTenantContext() {
        testTenantContext.clear();
    }

    /**
     * Get current tenant ID from context
     */
    protected UUID getCurrentTenantId() {
        return testTenantContext.getTenantId();
    }
}
