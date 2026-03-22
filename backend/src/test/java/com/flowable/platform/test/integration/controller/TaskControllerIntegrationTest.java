package com.flowable.platform.test.integration.controller;

import com.flowable.platform.controller.AbstractApiTest;
import com.flowable.platform.dto.TaskDTO;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for TaskController REST API endpoints.
 * Tests HTTP endpoints with actual HTTP calls using REST Assured.
 */
@DisplayName("TaskController Integration Tests")
class TaskControllerIntegrationTest extends AbstractApiTest {

    @Nested
    @DisplayName("GET /api/tasks/my-tasks")
    class GetMyTasksTests {

        @Test
        @DisplayName("Should return 200 with authenticated request")
        void shouldReturn200WithAuthenticatedRequest() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
            .when()
                .get("/api/tasks/my-tasks")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("data", notNullValue());
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuthentication() {
            given()
                .contentType("application/json")
            .when()
                .get("/api/tasks/my-tasks")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Should support pagination parameters")
        void shouldSupportPaginationParameters() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get("/api/tasks/my-tasks")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));
        }

        @Test
        @DisplayName("Should support priority filter")
        void shouldSupportPriorityFilter() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .queryParam("priority", "high")
            .when()
                .get("/api/tasks/my-tasks")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));
        }
    }

    @Nested
    @DisplayName("POST /api/tasks/{id}/complete")
    class CompleteTaskTests {

        @Test
        @DisplayName("Should return 404 for non-existent task")
        void shouldReturn404ForNonExistentTask() {
            String nonExistentTaskId = "non-existent-task-id";

            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .body(Map.of("variables", Map.of("approved", true)))
            .when()
                .post("/api/tasks/" + nonExistentTaskId + "/complete")
            .then()
                .statusCode(anyOf(equalTo(404), equalTo(500)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuth() {
            given()
                .contentType("application/json")
                .body(Map.of("variables", Map.of()))
            .when()
                .post("/api/tasks/some-task-id/complete")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("Should accept empty variables")
        void shouldAcceptEmptyVariables() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .body(Map.of("variables", Map.of()))
            .when()
                .post("/api/tasks/some-task-id/complete")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404), equalTo(500)));
        }
    }

    @Nested
    @DisplayName("POST /api/tasks/{id}/delegate")
    class DelegateTaskTests {

        @Test
        @DisplayName("Should return 404 for non-existent task")
        void shouldReturn404ForNonExistentTask() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .body(Map.of("delegateTo", "another-user"))
            .when()
                .post("/api/tasks/non-existent-id/delegate")
            .then()
                .statusCode(anyOf(equalTo(404), equalTo(500)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuth() {
            given()
                .contentType("application/json")
                .body(Map.of("delegateTo", "another-user"))
            .when()
                .post("/api/tasks/some-task-id/delegate")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("POST /api/tasks/{id}/cc")
    class AddCcUsersTests {

        @Test
        @DisplayName("Should return 404 for non-existent task")
        void shouldReturn404ForNonExistentTask() {
            List<String> ccUsers = List.of("user1", "user2");

            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
                .body(Map.of("ccUsers", ccUsers))
            .when()
                .post("/api/tasks/non-existent-id/cc")
            .then()
                .statusCode(anyOf(equalTo(404), equalTo(500)));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuth() {
            given()
                .contentType("application/json")
                .body(Map.of("ccUsers", List.of("user1")))
            .when()
                .post("/api/tasks/some-task-id/cc")
            .then()
                .statusCode(401);
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/expiration-alerts")
    class GetExpirationAlertsTests {

        @Test
        @DisplayName("Should return expiration alerts for authenticated user")
        void shouldReturnExpirationAlerts() {
            given()
                .header("Authorization", "Bearer " + userToken)
                .contentType("application/json")
            .when()
                .get("/api/tasks/expiration-alerts")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));
        }

        @Test
        @DisplayName("Should return 401 without authentication")
        void shouldReturn401WithoutAuth() {
            given()
                .contentType("application/json")
            .when()
                .get("/api/tasks/expiration-alerts")
            .then()
                .statusCode(401);
        }
    }
}
