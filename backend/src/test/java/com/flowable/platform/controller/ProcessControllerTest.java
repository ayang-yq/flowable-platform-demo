package com.flowable.platform.controller;

import com.flowable.platform.dto.ProcessDTO;
import com.flowable.platform.dto.StartProcessRequest;
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("ProcessController API Tests")
class ProcessControllerTest extends AbstractApiTest {

    @BeforeEach
    void deployTestProcesses() {
        try {
            File simpleProcess = new ClassPathResource("processes/simple-process.bpmn").getFile();
            deployProcessFile(simpleProcess);

            File userTaskProcess = new ClassPathResource("processes/user-task-process.bpmn").getFile();
            deployProcessFile(userTaskProcess);

            File parallelGateway = new ClassPathResource("processes/parallel-gateway.bpmn").getFile();
            deployProcessFile(parallelGateway);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deploy test processes", e);
        }
    }

    @AfterEach
    void cleanupProcesses() {
        List<ProcessDTO> instances = givenWithAuth(adminToken)
                .get("/api/processes")
                .then()
                .extract()
                .body()
                .jsonPath()
                .getList("data", ProcessDTO.class);

        instances.forEach(instance ->
                givenWithAuth(adminToken)
                        .post("/api/processes/" + instance.getId() + "/terminate")
                        .then()
                        .statusCode(200)
        );
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

    protected String startTestProcess() {
        return startTestProcess("simple-process");
    }

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
                .body("data.id", notNullValue())
                .extract()
                .response();

        return response.path("data.id");
    }

    @Test
    @DisplayName("GET /api/processes should return list of process instances")
    void listProcessInstances_returnsList() {
        startTestProcess();

        givenWithAuth(adminToken)
                .get("/api/processes")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/processes should start new process instance")
    void startProcess_validKey_returnsInstance() {
        StartProcessRequest request = new StartProcessRequest();
        request.setProcessDefinitionKey("simple-process");
        request.setVariables(Map.of());

        givenWithAuth(adminToken)
                .body(request)
                .when()
                .post("/api/processes")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.processDefinitionKey", is("simple-process"))
                .body("data.id", notNullValue());
    }

    @Test
    @DisplayName("POST /api/processes with invalid key returns error")
    void startProcess_invalidKey_returnsError() {
        StartProcessRequest request = new StartProcessRequest();
        request.setProcessDefinitionKey("non-existent-process");
        request.setVariables(Map.of());

        givenWithAuth(adminToken)
                .body(request)
                .when()
                .post("/api/processes")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/processes/{id} should return process instance details")
    void getProcessInstance_validId_returnsDetails() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.id", is(instanceId))
                .body("data.processDefinitionKey", is("simple-process"));
    }

    @Test
    @DisplayName("GET /api/processes/{id} with invalid ID returns 404")
    void getProcessInstance_invalidId_returns404() {
        givenWithAuth(adminToken)
                .get("/api/processes/non-existent-id")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/processes/{id}/suspend should suspend process")
    void suspendProcessInstance_worksCorrectly() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/suspend")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"));

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/processes/{id}/activate should activate suspended process")
    void activateProcessInstance_worksCorrectly() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/suspend")
                .then()
                .statusCode(200);

        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/activate")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"));

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(200)
                .body("data.id", is(instanceId));
    }

    @Test
    @DisplayName("POST /api/processes/{id}/terminate should delete process")
    void terminateProcessInstance_deletesInstance() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/terminate")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"));

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /api/processes/{id}/tasks should return tasks")
    void getProcessTasks_returnsTaskList() {
        String instanceId = startTestProcess("user-task-process");

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId + "/tasks")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("GET /api/processes/definitions should return process definitions")
    void listProcessDefinitions_returnsList() {
        givenWithAuth(adminToken)
                .get("/api/processes/definitions")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/processes/definitions with valid BPMN file should deploy")
    void deployProcessDefinition_validBpmnFile_success() throws Exception {
        File bpmnFile = new ClassPathResource("processes/simple-process.bpmn").getFile();

        givenWithAuth(adminToken)
                .multiPart("file", bpmnFile)
                .when()
                .post("/api/processes/definitions")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data.deploymentId", notNullValue())
                .body("data.message", is("Process definition deployed successfully"));
    }

    @Test
    @DisplayName("POST /api/processes/definitions with empty file returns 400")
    void deployProcessDefinition_emptyFile_returns400() throws Exception {
        File emptyFile = File.createTempFile("empty", ".bpmn");
        emptyFile.deleteOnExit();

        givenWithAuth(adminToken)
                .multiPart("file", emptyFile)
                .when()
                .post("/api/processes/definitions")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /api/processes/definitions with unsupported file type returns 400")
    void deployProcessDefinition_unsupportedFileType_returns400() throws Exception {
        File textFile = File.createTempFile("test", ".txt");
        textFile.deleteOnExit();

        givenWithAuth(adminToken)
                .multiPart("file", textFile)
                .when()
                .post("/api/processes/definitions")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("GET /api/processes/{id}/diagram should return SVG diagram")
    void getProcessDiagram_returnsSvg() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId + "/diagram")
                .then()
                .statusCode(200)
                .body("code", is("SUCCESS"))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/processes/cases/{id}/ad-hoc-tasks with missing task name returns 400")
    void createAdHocTask_missingTaskName_returns400() {
        Map<String, Object> requestBody = Map.of(
                "description", "No task name"
        );

        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/api/processes/cases/mock-case-id/ad-hoc-tasks")
                .then()
                .statusCode(400)
                .body("code", is("VALIDATION_ERROR"));
    }
}
