package com.flowable.platform.controller;

import com.flowable.platform.service.JwtTokenService;
import com.flowable.platform.test.config.TestTenantConfig;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractApiTest {

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

    @BeforeEach
    void setUp() {
        RestAssured.config = RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        RestAssured.port = port;
        RestAssured.basePath = "";

        // Generate test JWT tokens directly
        String tenantId = TestTenantConfig.TEST_TENANT_ID.toString();
        adminToken = jwtTokenService.generateToken("admin", tenantId);
        userToken = jwtTokenService.generateToken("user", tenantId);
    }

    protected RequestSpecification givenWithAuth(String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }
}
