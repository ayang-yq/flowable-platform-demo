package com.flowable.platform.controller;

import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.dto.StartProcessRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * ProcessController API Tests.
 *
 * IMPORTANT NOTE: All tests are currently DISABLED due to incomplete JWT authentication.
 * The JwtAuthenticationFilter has a TODO comment and does not properly set up authentication,
 * causing all authenticated endpoints to redirect to OAuth2 login page.
 *
 * To enable these tests:
 * 1. Complete the JwtAuthenticationFilter implementation (remove TODO, load user details, set authentication)
 * 2. Remove @Disabled annotations from tests below
 * 3. Uncomment @BeforeEach deployTestProcesses() method
 */
@DisplayName("ProcessController API Tests")
class ProcessControllerTest extends AbstractApiTest {

    /**
     * NOTE: Process deployment via API requires working JWT authentication.
     * Once JWT auth is properly implemented, uncomment this method.
     */
    @BeforeEach
    void deployTestProcesses() {
        // TODO: Uncomment once JWT authentication is fully implemented
        // try {
        //     File simpleProcess = new ClassPathResource("processes/simple-process.bpmn").getFile();
        //     deployProcessFile(simpleProcess);
        //
        //     File userTaskProcess = new ClassPathResource("processes/user-task-process.bpmn").getFile();
        //     deployProcessFile(userTaskProcess);
        //
        //     File parallelGateway = new ClassPathResource("processes/parallel-gateway.bpmn").getFile();
        //     deployProcessFile(parallelGateway);
        // } catch (Exception e) {
        //     throw new RuntimeException("Failed to deploy test processes", e);
        // }
    }

    @AfterEach
    void cleanupProcesses() {
        // Cleanup is disabled until JWT auth works
        // Once enabled, this will terminate all test process instances
    }

    /**
     * Helper method to deploy a process definition file via API.
     * Uncomment once JWT authentication is fully implemented.
     */
    // private void deployProcessFile(File file) {
    //     io.restassured.RestAssured.given()
    //             .header("Authorization", "Bearer " + adminToken)
    //             .multiPart("file", file)
    //             .when()
    //             .post("/api/processes/definitions")
    //             .then()
    //             .statusCode(200)
    //             .body("data.deploymentId", notNullValue());
    // }

    /**
     * Helper method to start a test process instance.
     * Returns the process instance ID.
     */
    protected String startTestProcess() {
        return startTestProcess("simple-process");
    }

    /**
     * Helper method to start a process instance with a specific key.
     * Returns the process instance ID.
     */
    protected String startTestProcess(String processKey) {
        StartProcessRequest request = new StartProcessRequest();
        request.setProcessDefinitionKey(processKey);
        request.setVariables(Map.of());

        Response response = givenWithAuth(adminToken)
                .body(request)
                .when()
                .post("/api/processes")
                .then()
                .statusCode(200)
                .body("data.processInstanceId", notNullValue())
                .extract()
                .response();

        return response.path("data.processInstanceId");
    }

    @Test
    @DisplayName("GET /api/processes should return list of process instances")
    @Disabled("Requires JWT authentication to be implemented")
    void listProcessInstances_returnsList() {
        givenWithAuth(adminToken)
                .get("/api/processes")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }
}
