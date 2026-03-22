package com.flowable.platform.controller;

import com.flowable.platform.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.hamcrest.Matchers.notNullValue;

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

        // Login as admin
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUsername("admin");
        adminLogin.setTenantCode("tenant-1");
        adminLogin.setPassword("admin123");

        Response adminResponse = RestAssured.given()
                .contentType("application/json")
                .body(adminLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("data.token", notNullValue())
                .extract()
                .response();

        adminToken = adminResponse.path("data.token");

        // Login as regular user
        LoginRequest userLogin = new LoginRequest();
        userLogin.setUsername("user");
        userLogin.setTenantCode("tenant-1");
        userLogin.setPassword("user123");

        Response userResponse = RestAssured.given()
                .contentType("application/json")
                .body(userLogin)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("data.token", notNullValue())
                .extract()
                .response();

        userToken = userResponse.path("data.token");
    }

    protected RequestSpecification givenWithAuth(String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }
}
