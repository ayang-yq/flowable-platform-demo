package com.flowable.platform.controller;

import com.flowable.platform.dto.TaskDTO;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;

@DisplayName("TaskController API Tests")
class TaskControllerTest extends AbstractApiTest {

    private String processInstanceId;

    @BeforeEach
    void deployTestProcessAndCreateTasks() {
        try {
            // Deploy a user task process
            File userTaskProcess = new ClassPathResource("processes/user-task-process.bpmn").getFile();
            deployProcessFile(userTaskProcess);

            // Start a process instance to create tasks
            Map<String, Object> request = Map.of(
                    "processDefinitionKey", "user-task-process",
                    "variables", Map.of()
            );

            Response response = givenWithAuth(adminToken)
                    .body(request)
                    .when()
                    .post("/api/processes")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            processInstanceId = response.path("data.processInstanceId");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test process", e);
        }
    }

    @AfterEach
    void cleanup() {
        if (processInstanceId != null) {
            givenWithAuth(adminToken)
                    .post("/api/processes/" + processInstanceId + "/terminate")
                    .then()
                    .statusCode(200);
        }
    }

    private void deployProcessFile(File file) {
        givenWithAuth(adminToken)
                .multiPart("file", file)
                .when()
                .post("/api/processes/definitions")
                .then()
                .statusCode(200)
                .body("data.deploymentId", notNullValue());
    }

    @Test
    @DisplayName("GET /api/tasks/my-tasks should return user's assigned tasks")
    void getMyTasks_returnsUserTasks() {
        givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/tasks/completed should return user's completed tasks")
    void getCompletedTasks_returnsCompletedTasks() {
        givenWithAuth(userToken)
                .get("/api/tasks/completed")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/tasks/my-requests should return process instances initiated by user")
    void getMyRequests_returnsUserRequests() {
        givenWithAuth(userToken)
                .get("/api/tasks/my-requests")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/tasks/my-tasks with pagination should return paginated results")
    void getMyTasks_withPagination_returnsPaginatedResults() {
        givenWithAuth(userToken)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .get("/api/tasks/my-tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/claim should claim unassigned task")
    void claimTask_unassignedTask_success() {
        // Get a task first
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            givenWithAuth(userToken)
                    .post("/api/tasks/" + taskId + "/claim")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("Task claimed successfully"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/claim with already claimed task returns error")
    void claimTask_alreadyClaimed_returnsError() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            // Claim the task first
            givenWithAuth(userToken)
                    .post("/api/tasks/" + taskId + "/claim")
                    .then()
                    .statusCode(200);

            // Try to claim again
            givenWithAuth(userToken)
                    .post("/api/tasks/" + taskId + "/claim")
                    .then()
                    .statusCode(400)
                    .body("code", is("VALIDATION_ERROR"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/complete should complete task with variables")
    void completeTask_withVariables_success() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "variables", Map.of(
                            "approved", true,
                            "comment", "Task completed successfully"
                    )
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/complete")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("Task completed successfully"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/complete with missing required variable returns error")
    void completeTask_missingRequiredVariable_returnsError() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "variables", Map.of() // Missing required variables
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/complete")
                    .then()
                    .statusCode(400)
                    .body("code", is("VALIDATION_ERROR"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/delegate should delegate task to another user")
    void delegateTask_toAnotherUser_success() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "delegateTo", "admin"
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/delegate")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("Task delegated successfully"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/delegate with invalid user returns error")
    void delegateTask_invalidUser_returnsError() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "delegateTo", "nonexistentuser"
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/delegate")
                    .then()
                    .statusCode(404)
                    .body("code", is("NOT_FOUND"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/reassign should reassign task (admin only)")
    void reassignTask_admin_success() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "assignee", "admin"
            );

            givenWithAuth(adminToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/reassign")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("Task reassigned successfully"));
        }
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/reassign with non-admin returns 403")
    void reassignTask_nonAdmin_returns403() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "assignee", "admin"
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/reassign")
                    .then()
                    .statusCode(403);
        }
    }

    @Test
    @DisplayName("GET /api/tasks/all should return all tasks (admin only)")
    void getAllTasks_admin_success() {
        givenWithAuth(adminToken)
                .get("/api/tasks/all")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/tasks/all with non-admin returns 403")
    void getAllTasks_nonAdmin_returns403() {
        givenWithAuth(userToken)
                .get("/api/tasks/all")
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/tasks/{id} should return task details")
    void getTaskDetails_validId_returnsDetails() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            givenWithAuth(userToken)
                    .get("/api/tasks/" + taskId)
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.id", is(taskId))
                    .body("data.name", notNullValue())
                    .body("data.processInstanceId", notNullValue());
        }
    }

    @Test
    @DisplayName("GET /api/tasks/{id} with invalid ID returns 404")
    void getTaskDetails_invalidId_returns404() {
        givenWithAuth(userToken)
                .get("/api/tasks/non-existent-id")
                .then()
                .statusCode(404)
                .body("code", is("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/tasks/my-tasks with department filter")
    void getMyTasks_withDepartmentFilter_returnsFilteredTasks() {
        givenWithAuth(userToken)
                .queryParam("department", "engineering")
                .get("/api/tasks/my-tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("GET /api/tasks/my-tasks with priority filter")
    void getMyTasks_withPriorityFilter_returnsFilteredTasks() {
        givenWithAuth(userToken)
                .queryParam("priority", "high")
                .get("/api/tasks/my-tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("GET /api/tasks/my-tasks with due date filter")
    void getMyTasks_withDueDateFilter_returnsFilteredTasks() {
        givenWithAuth(userToken)
                .queryParam("dueBefore", "2026-12-31")
                .get("/api/tasks/my-tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/tasks/{id}/cc should add CC users to task")
    void addCcUsers_validUsers_success() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            Map<String, Object> requestBody = Map.of(
                    "ccUsers", List.of("user1", "user2")
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/cc")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("CC users added successfully"));
        }
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id}/cc/{username} should remove CC user")
    void removeCcUser_validUser_success() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            // First add a CC user
            Map<String, Object> requestBody = Map.of(
                    "ccUsers", List.of("user1")
            );

            givenWithAuth(userToken)
                    .body(requestBody)
                    .post("/api/tasks/" + taskId + "/cc")
                    .then()
                    .statusCode(200);

            // Then remove them
            givenWithAuth(userToken)
                    .delete("/api/tasks/" + taskId + "/cc/user1")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.message", is("CC user removed successfully"));
        }
    }

    @Test
    @DisplayName("GET /api/tasks/{id}/cc should return CC users for task")
    void getCcUsers_validTask_returnsCcUsers() {
        List<TaskDTO> tasks = givenWithAuth(userToken)
                .get("/api/tasks/my-tasks")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", TaskDTO.class);

        if (!tasks.isEmpty()) {
            String taskId = tasks.get(0).getId();

            givenWithAuth(userToken)
                    .get("/api/tasks/" + taskId + "/cc")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data", notNullValue())
                    .body("data", isA(List.class));
        }
    }
}
