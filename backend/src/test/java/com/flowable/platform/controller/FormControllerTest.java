package com.flowable.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("FormController API Tests")
class FormControllerTest extends AbstractApiTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String formId;

    @BeforeEach
    void createTestForms() {
        // Create a test form schema
        Map<String, Object> formSchema = Map.of(
                "title", "Leave Request Form",
                "description", "Submit a leave request",
                "fields", List.of(
                        Map.of(
                                "name", "employeeName",
                                "type", "text",
                                "label", "Employee Name",
                                "required", true
                        ),
                        Map.of(
                                "name", "reason",
                                "type", "textarea",
                                "label", "Reason",
                                "required", true
                        )
                )
        );

        try {
            String schemaJson = objectMapper.writeValueAsString(formSchema);

            Response response = givenWithAuth(adminToken)
                    .body(Map.of(
                            "name", "Leave Request Form",
                            "description", "Form for leave requests",
                            "schema", schemaJson
                    ))
                    .when()
                    .post("/api/forms")
                    .then()
                    .statusCode(200)
                    .extract()
                    .response();

            formId = response.path("data.id");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test form", e);
        }
    }

    @AfterEach
    void cleanupForms() {
        if (formId != null) {
            givenWithAuth(adminToken)
                    .delete("/api/forms/" + formId)
                    .then()
                    .statusCode(200);
        }
    }

    @Test
    @DisplayName("POST /api/forms should create new form schema")
    void createForm_validSchema_returnsCreatedForm() throws Exception {
        Map<String, Object> formSchema = Map.of(
                "title", "Test Form",
                "fields", List.of()
        );

        String schemaJson = objectMapper.writeValueAsString(formSchema);

        Map<String, Object> requestBody = Map.of(
                "name", "Test Form",
                "description", "Test form description",
                "schema", schemaJson
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/api/forms")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.id", notNullValue())
                .body("data.name", is("Test Form"))
                .body("data.version", is(1));
    }

    @Test
    @DisplayName("POST /api/forms with invalid JSON returns 400")
    void createForm_invalidJson_returns400() {
        Map<String, Object> requestBody = Map.of(
                "name", "Invalid Form",
                "description", "Form with invalid schema",
                "schema", "invalid json {{{"
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/api/forms")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/forms with schema exceeding size limit returns 400")
    void createForm_schemaExceedsLimit_returns400() throws Exception {
        // Create schema larger than 10KB
        StringBuilder largeSchema = new StringBuilder();
        largeSchema.append("{\"title\":\"Large Form\",\"fields\":[");
        for (int i = 0; i < 1000; i++) {
            if (i > 0) largeSchema.append(",");
            largeSchema.append("{\"name\":\"field").append(i).append("\",\"type\":\"text\"}");
        }
        largeSchema.append("]}");

        Map<String, Object> requestBody = Map.of(
                "name", "Large Form",
                "schema", largeSchema.toString()
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/api/forms")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/forms should return list of forms")
    void getForms_returnsList() {
        givenWithAuth(adminToken)
                .get("/api/forms")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/forms/{id} should return form details")
    void getFormById_validId_returnsForm() {
        givenWithAuth(adminToken)
                .get("/api/forms/" + formId)
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.id", is(formId))
                .body("data.name", is("Leave Request Form"))
                .body("data.schema", notNullValue());
    }

    @Test
    @DisplayName("GET /api/forms/{id} with invalid ID returns 404")
    void getFormById_invalidId_returns404() {
        givenWithAuth(adminToken)
                .get("/api/forms/non-existent-id")
                .then()
                .statusCode(404)
                .body("code", is("NOT_FOUND"));
    }

    @Test
    @DisplayName("PUT /api/forms/{id} should create new version")
    void updateForm_validSchema_createsNewVersion() throws Exception {
        Map<String, Object> updatedSchema = Map.of(
                "title", "Updated Leave Request Form",
                "fields", List.of(
                        Map.of("name", "employeeName", "type", "text"),
                        Map.of("name", "duration", "type", "number")
                )
        );

        String schemaJson = objectMapper.writeValueAsString(updatedSchema);

        Map<String, Object> requestBody = Map.of(
                "schema", schemaJson
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .put("/api/forms/" + formId)
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.version", is(2)); // New version created
    }

    @Test
    @DisplayName("PUT /api/forms/{id} with invalid ID returns 404")
    void updateForm_invalidId_returns404() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "schema", "{\"title\":\"Test\"}"
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .put("/api/forms/non-existent-id")
                .then()
                .statusCode(404)
                .body("code", is("NOT_FOUND"));
    }

    @Test
    @DisplayName("DELETE /api/forms/{id} should deactivate form")
    void deleteForm_validId_deactivatesForm() {
        givenWithAuth(adminToken)
                .delete("/api/forms/" + formId)
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.message", is("Form deactivated successfully"));
    }

    @Test
    @DisplayName("DELETE /api/forms/{id} with invalid ID returns 404")
    void deleteForm_invalidId_returns404() {
        givenWithAuth(adminToken)
                .delete("/api/forms/non-existent-id")
                .then()
                .statusCode(404)
                .body("code", is("NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/forms/{id}/versions should return form version history")
    void getFormVersions_validId_returnsVersionHistory() {
        givenWithAuth(adminToken)
                .get("/api/forms/" + formId + "/versions")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("GET /api/forms/{id}/validate should validate form data")
    void validateFormData_validData_returnsValidationResult() throws Exception {
        Map<String, Object> formData = Map.of(
                "employeeName", "John Doe",
                "reason", "Annual leave"
        );

        givenWithAuth(adminToken)
                .body(formData)
                .when()
                .post("/api/forms/" + formId + "/validate")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.valid", is(true));
    }

    @Test
    @DisplayName("GET /api/forms/{id}/validate with missing required fields returns validation errors")
    void validateFormData_missingRequiredFields_returnsErrors() throws Exception {
        Map<String, Object> formData = Map.of(
                "employeeName", "John Doe"
                // Missing "reason" which is required
        );

        givenWithAuth(adminToken)
                .body(formData)
                .when()
                .post("/api/forms/" + formId + "/validate")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.valid", is(false))
                .body("data.errors", notNullValue());
    }

    @Test
    @DisplayName("GET /api/forms/process-instance/{processInstanceId} should return form for process instance")
    void getFormForProcessInstance_validInstanceId_returnsForm() throws Exception {
        // First deploy a process and start an instance
        try {
            File processFile = new ClassPathResource("processes/simple-process.bpmn").getFile();
            givenWithAuth(adminToken)
                    .multiPart("file", processFile)
                    .when()
                    .post("/api/processes/definitions")
                    .then()
                    .statusCode(200);
        } catch (Exception e) {
            // Process might already be deployed
        }

        // Start a process instance with form data
        Map<String, Object> variables = Map.of(
                "formId", formId,
                "formVersion", 1
        );

        Response response = givenWithAuth(adminToken)
                .body(Map.of(
                        "processDefinitionKey", "simple-process",
                        "variables", variables
                ))
                .when()
                .post("/api/processes")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String processInstanceId = response.path("data.processInstanceId");
        assertNotNull(processInstanceId);

        // Get form for process instance
        givenWithAuth(adminToken)
                .get("/api/forms/process-instance/" + processInstanceId)
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.id", is(formId))
                .body("data.version", is(1));
    }

    @Test
    @DisplayName("GET /api/forms/active should return only active forms")
    void getActiveForms_returnsActiveFormsOnly() {
        givenWithAuth(adminToken)
                .get("/api/forms/active")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue())
                .body("data", isA(List.class));
    }

    @Test
    @DisplayName("POST /api/forms/{id}/validate should enforce field-level permissions")
    void validateFormData_fieldLevelPermissions_enforcesPermissions() throws Exception {
        // Create a form with field-level permissions
        Map<String, Object> schemaWithPermissions = Map.of(
                "title", "Permission Form",
                "fieldPermissions", Map.of(
                        "employeeName", Map.of(
                                "permission", "read-only",
                                "roles", List.of("user", "manager")
                        ),
                        "reason", Map.of(
                                "permission", "editable",
                                "roles", List.of("manager")
                        )
                ),
                "fields", List.of()
        );

        String schemaJson = objectMapper.writeValueAsString(schemaWithPermissions);

        Map<String, Object> createRequest = Map.of(
                "name", "Permission Form",
                "schema", schemaJson
        );

        Response response = givenWithAuth(adminToken)
                .body(createRequest)
                .when()
                .post("/api/forms")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String permissionFormId = response.path("data.id");

        try {
            // Try to validate data with a regular user (not manager)
            Map<String, Object> formData = Map.of(
                    "employeeName", "Jane Doe",  // Read-only field
                    "reason", "Test"             // Manager-only field
            );

            // Validation should succeed but note permission restrictions
            givenWithAuth(userToken)
                    .body(formData)
                    .when()
                    .post("/api/forms/" + permissionFormId + "/validate")
                    .then()
                    .statusCode(200)
                    .body("code", is("SUCCESS"))
                    .body("data.permissionWarnings", notNullValue());
        } finally {
            // Cleanup
            givenWithAuth(adminToken)
                    .delete("/api/forms/" + permissionFormId);
        }
    }
}
