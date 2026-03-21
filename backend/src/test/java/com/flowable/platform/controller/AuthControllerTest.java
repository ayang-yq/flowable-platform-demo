package com.flowable.platform.controller;

import com.flowable.platform.dto.LoginRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("AuthController API Tests")
class AuthControllerTest extends AbstractApiTest {

    @Test
    @DisplayName("POST /api/auth/login with valid credentials returns JWT token")
    void login_validCredentials_returnsToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setTenantCode("tenant-1");
        request.setPassword("admin123");

        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.token", notNullValue())
                .body("data.username", is("admin"))
                .body("data.tenantCode", is("tenant-1"));
    }

    @Test
    @DisplayName("POST /api/auth/login with invalid password returns error")
    void login_invalidCredentials_returnsError() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setTenantCode("tenant-1");
        request.setPassword("wrongpassword");

        RestAssured.given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(400); // Bad Request for invalid credentials
    }

    @Test
    @DisplayName("POST /api/auth/logout returns success")
    void logout_success_returnsOk() {
        givenWithAuth(adminToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"));
    }

    @Test
    @DisplayName("GET /api/auth/me returns user info")
    void getCurrentUser_returnsUserInfo() {
        givenWithAuth(adminToken)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }
}
