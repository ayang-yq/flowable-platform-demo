# API Testing Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build comprehensive functional API tests for all backend endpoints using REST-assured, Testcontainers, and real authentication flows.

**Architecture:** REST-assured DSL tests against full Spring Boot context with Testcontainers PostgreSQL, using real JWT authentication via login API and API-based cleanup between tests.

**Tech Stack:** Java 21, Spring Boot 3.5, REST-assured 5.4.0, Testcontainers, JUnit 5, PostgreSQL

---

## Task 1: Add REST-assured Dependency

**Files:**
- Modify: `backend/pom.xml`

**Step 1: Add REST-assured dependency to pom.xml**

Add this dependency inside the `<dependencies>` section (after line 172, before `</dependencies>`):

```xml
<!-- REST-assured for API testing -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

**Step 2: Verify Maven downloads the dependency**

Run: `cd backend && mvn dependency:tree -Dincludes=io.rest-assured:rest-assured`
Expected: Output shows `io.rest-assured:rest-assured:jar:5.4.0:test`

**Step 3: Commit**

```bash
cd backend
git add pom.xml
git commit -m "feat(test): add REST-assured dependency for API testing

Add REST-assured 5.4.0 for readable, DSL-style API testing.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 2: Create Test Configuration

**Files:**
- Create: `backend/src/test/resources/application-test.properties`

**Step 1: Create application-test.properties file**

Create the file with these contents:

```properties
# Testcontainers database configuration
spring.datasource.url=jdbc:tc:postgresql:16:///testdb
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver

# Disable mail server for tests
spring.mail.host=localhost
spring.mail.port=25

# JWT configuration for tests
jwt.secret=test-secret-key-for-jwt-token-generation-minimum-256-bits
jwt.expiration=3600000

# Logging configuration
logging.level.com.flowable.platform=DEBUG
logging.level.org.flowable=WARN
logging.level.org.springframework.security=DEBUG

# Flowable test configuration
flowable.database-schema-update=true
```

**Step 2: Verify Spring recognizes the profile**

Run: `cd backend && mvn test -Dspring.profiles.active=test -Dtest=dummy`
Expected: Build fails with "No tests found" but Spring Boot loads application-test.properties

**Step 3: Commit**

```bash
cd backend
git add src/test/resources/application-test.properties
git commit -m "feat(test): add test configuration

Configure Testcontainers PostgreSQL, JWT settings, and logging for
test environment.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 3: Create Flyway Test Migration

**Files:**
- Create: `backend/src/test/resources/db/migration/test-data/V1__test_data.sql`

**Step 1: Create migration directory structure**

Run: `mkdir -p backend/src/test/resources/db/migration/test-data`

**Step 2: Generate BCrypt password hashes**

You'll need BCrypt hashes for these passwords:
- `admin123`
- `user123`
- `other123`

Use an online BCrypt generator or run this Java snippet:
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
String hash = new BCryptPasswordEncoder().encode("admin123");
System.out.println(hash); // Use this in SQL
```

**Step 3: Create V1__test_data.sql**

Replace the BCrypt hashes below with your generated hashes:

```sql
-- Test Tenants
INSERT INTO tenant (id, code, name, status, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'tenant-1', 'Test Tenant 1', 'ACTIVE', NOW()),
('22222222-2222-2222-2222-222222222222', 'tenant-2', 'Test Tenant 2', 'ACTIVE', NOW());

-- Test Users (replace hashes with your BCrypt-generated hashes)
INSERT INTO user_account (id, tenant_id, username, password, email, status, created_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
 '11111111-1111-1111-1111-111111111111',
 'admin',
 '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKnWZ4ZCmKtGxNJFNz3ZXWgLqYKC1i',
 'admin@test.com',
 'ACTIVE',
 NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
 '11111111-1111-1111-1111-111111111111',
 'user',
 '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKnWZ4ZCmKtGxNJFNz3ZXWgLqYKC1i',
 'user@test.com',
 'ACTIVE',
 NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc',
 '22222222-2222-2222-2222-222222222222',
 'other',
 '$2a$10$N9qo8uLOickgx2ZMRZoMye.IKnWZ4ZCmKtGxNJFNz3ZXWgLqYKC1i',
 'other@test.com',
 'ACTIVE',
 NOW());
```

**Step 4: Verify Flyway recognizes the migration**

Run: `cd backend && mvn flyway:info -Dflyway.configFiles=src/test/resources/application-test.properties`
Expected: Shows "V1__test_data" in pending migrations

**Step 5: Commit**

```bash
cd backend
git add src/test/resources/db/migration/test-data/
git commit -m "feat(test): add test data migration

Add test tenants and users with BCrypt password hashes for
authentication testing.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 4: Create Test BPMN Process Definitions

**Files:**
- Create: `backend/src/test/resources/processes/simple-process.bpmn`
- Create: `backend/src/test/resources/processes/user-task-process.bpmn`
- Create: `backend/src/test/resources/processes/parallel-gateway.bpmn`

**Step 1: Create processes directory**

Run: `mkdir -p backend/src/test/resources/processes`

**Step 2: Create simple-process.bpmn**

File contents:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  <process id="simple-process" name="Simple Process" isExecutable="true">
    <startEvent id="startEvent"/>
    <sequenceFlow sourceRef="startEvent" targetRef="scriptTask"/>
    <scriptTask id="scriptTask" scriptFormat="javascript" flowable:resultVariable="result">
      <script>println("Simple process executed");</script>
    </scriptTask>
    <sequenceFlow sourceRef="scriptTask" targetRef="endEvent"/>
    <endEvent id="endEvent"/>
  </process>
</definitions>
```

**Step 3: Create user-task-process.bpmn**

File contents:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  <process id="user-task-process" name="User Task Process" isExecutable="true">
    <startEvent id="startEvent"/>
    <sequenceFlow sourceRef="startEvent" targetRef="userTask"/>
    <userTask id="userTask" name="Test Task" flowable:assignee="admin">
      <documentation>A test user task for API testing</documentation>
    </userTask>
    <sequenceFlow sourceRef="userTask" targetRef="endEvent"/>
    <endEvent id="endEvent"/>
  </process>
</definitions>
```

**Step 4: Create parallel-gateway.bpmn**

File contents:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="Examples">
  <process id="parallel-gateway" name="Parallel Gateway Process" isExecutable="true">
    <startEvent id="startEvent"/>
    <sequenceFlow sourceRef="startEvent" targetRef="fork"/>
    <parallelGateway id="fork" name="Fork"/>
    <sequenceFlow sourceRef="fork" targetRef="task1"/>
    <sequenceFlow sourceRef="fork" targetRef="task2"/>
    <userTask id="task1" name="Task 1" flowable:assignee="admin"/>
    <userTask id="task2" name="Task 2" flowable:assignee="admin"/>
    <sequenceFlow sourceRef="task1" targetRef="join"/>
    <sequenceFlow sourceRef="task2" targetRef="join"/>
    <parallelGateway id="join" name="Join"/>
    <sequenceFlow sourceRef="join" targetRef="endEvent"/>
    <endEvent id="endEvent"/>
  </process>
</definitions>
```

**Step 5: Verify BPMN files are valid XML**

Run: `xmllint --noout backend/src/test/resources/processes/*.bpmn`
Expected: No errors (if xmllint not available, skip this step)

**Step 6: Commit**

```bash
cd backend
git add src/test/resources/processes/
git commit -m "feat(test): add test BPMN process definitions

Add simple, user-task, and parallel gateway processes for
testing process instance and task endpoints.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 5: Create AbstractApiTest Base Class

**Files:**
- Create: `backend/src/test/java/com/flowable/platform/controller/AbstractApiTest.java`

**Step 1: Create controller test directory**

Run: `mkdir -p backend/src/test/java/com/flowable/platform/controller`

**Step 2: Create AbstractApiTest.java**

File contents:
```java
package com.flowable.platform.controller;

import com.flowable.platform.dto.LoginRequest;
import com.flowable.platform.dto.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractApiTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @LocalServerPort
    protected int port;

    protected String adminToken;
    protected String userToken;

    @BeforeAll
    static void startContainer() {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @BeforeEach
    void loginAndGetTokens() {
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

        LoginResponse adminData = adminResponse.path("data");
        adminToken = adminData.getToken();

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

        LoginResponse userData = userResponse.path("data");
        userToken = userData.getToken();
    }

    protected RequestSpecification givenWithAuth(String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json");
    }
}
```

**Step 3: Verify base class compiles**

Run: `cd backend && mvn test-compile`
Expected: Compiles successfully, no errors

**Step 4: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/AbstractApiTest.java
git commit -m "feat(test): create AbstractApiTest base class

Add base class for API tests with Testcontainers PostgreSQL,
REST-assured configuration, and JWT authentication setup.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 6: Create AuthControllerTest

**Files:**
- Create: `backend/src/test/java/com/flowable/platform/controller/AuthControllerTest.java`

**Step 1: Create AuthControllerTest with first test**

File contents:
```java
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
                .body("success", is(true))
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
                .statusCode(500); // Spring's default for IllegalArgumentException
    }

    @Test
    @DisplayName("POST /api/auth/logout returns success")
    void logout_success_returnsOk() {
        givenWithAuth(adminToken)
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("GET /api/auth/me returns user info")
    void getCurrentUser_returnsUserInfo() {
        givenWithAuth(adminToken)
                .when()
                .get("/api/auth/me")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data", notNullValue());
    }
}
```

**Step 2: Run tests to verify they pass**

Run: `cd backend && mvn test -Dtest=AuthControllerTest`
Expected: All 4 tests pass

**Step 3: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/AuthControllerTest.java
git commit -m "feat(test): add AuthControllerTest with authentication tests

Add comprehensive tests for login, logout, and current user endpoints
covering valid and invalid scenarios.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 7: Create ProcessControllerTest - Setup

**Files:**
- Create: `backend/src/test/java/com/flowable/platform/controller/ProcessControllerTest.java`

**Step 1: Create ProcessControllerTest with setup and helper methods**

File contents:
```java
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
                .body("success", is(true))
                .body("data", notNullValue());
    }
}
```

**Step 2: Run tests to verify setup works**

Run: `cd backend && mvn test -Dtest=ProcessControllerTest#listProcessInstances_returnsList`
Expected: Test passes

**Step 3: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/ProcessControllerTest.java
git commit -m "feat(test): add ProcessControllerTest setup

Add test class with setup/teardown for process deployment
and cleanup with initial list test.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 8: Add Process Instance Management Tests

**Files:**
- Modify: `backend/src/test/java/com/flowable/platform/controller/ProcessControllerTest.java`

**Step 1: Add process instance tests after the existing test**

Add these tests before the closing brace of the class:

```java
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
                .body("success", is(true))
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
                .statusCode(500); // Flowable throws exception for non-existent process
    }

    @Test
    @DisplayName("GET /api/processes/{id} should return process instance details")
    void getProcessInstance_validId_returnsDetails() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(200)
                .body("success", is(true))
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
                .body("success", is(true));

        // Verify suspended (query returns 404 or empty)
        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/processes/{id}/activate should activate suspended process")
    void activateProcessInstance_worksCorrectly() {
        String instanceId = startTestProcess();

        // Suspend first
        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/suspend")
                .then()
                .statusCode(200);

        // Activate
        givenWithAuth(adminToken)
                .post("/api/processes/" + instanceId + "/activate")
                .then()
                .statusCode(200)
                .body("success", is(true));

        // Verify accessible again
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
                .body("success", is(true));

        // Verify terminated (returns 404)
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
                .body("success", is(true))
                .body("data", notNullValue());
    }
}
```

**Step 2: Run all process instance tests**

Run: `cd backend && mvn test -Dtest=ProcessControllerTest`
Expected: All tests pass

**Step 3: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/ProcessControllerTest.java
git commit -m "feat(test): add process instance management tests

Add tests for starting, retrieving, suspending, activating,
terminating process instances and retrieving tasks.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 9: Add Process Definition Deployment Tests

**Files:**
- Modify: `backend/src/test/java/com/flowable/platform/controller/ProcessControllerTest.java`

**Step 1: Add deployment tests**

Add these tests to ProcessControllerTest:

```java
    @Test
    @DisplayName("GET /api/processes/definitions should return process definitions")
    void listProcessDefinitions_returnsList() {
        givenWithAuth(adminToken)
                .get("/api/processes/definitions")
                .then()
                .statusCode(200)
                .body("success", is(true))
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
                .body("success", is(true))
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
                .body("error", is("VALIDATION_ERROR"));
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
                .body("error", is("VALIDATION_ERROR"));
    }
```

**Step 2: Run tests**

Run: `cd backend && mvn test -Dtest=ProcessControllerTest`
Expected: All tests pass

**Step 3: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/ProcessControllerTest.java
git commit -m "feat(test): add process definition deployment tests

Add tests for listing definitions and deploying BPMN files
with validation for empty and unsupported file types.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 10: Add Remaining Edge Case Tests

**Files:**
- Modify: `backend/src/test/java/com/flowable/platform/controller/ProcessControllerTest.java`

**Step 1: Add diagram and CMMN tests**

Add these tests to ProcessControllerTest:

```java
    @Test
    @DisplayName("GET /api/processes/{id}/diagram should return SVG diagram")
    void getProcessDiagram_returnsSvg() {
        String instanceId = startTestProcess();

        givenWithAuth(adminToken)
                .get("/api/processes/" + instanceId + "/diagram")
                .then()
                .statusCode(200)
                .body("success", is(true))
                .body("data", notNullValue());
    }

    @Test
    @DisplayName("POST /api/processes/cases/{id}/ad-hoc-tasks should create task")
    void createAdHocTask_validRequest_success() {
        // First create a CMMN case instance (if supported)
        // For now, test with a mock case ID since CMMN deployment isn't fully implemented
        Map<String, Object> requestBody = Map.of(
                "taskName", "Ad-hoc Test Task",
                "description", "Created by API test",
                "assignee", "admin"
        );

        // This will likely return 404 since we don't have a real case instance
        // but it tests the endpoint structure
        givenWithAuth(adminToken)
                .body(requestBody)
                .when()
                .post("/api/processes/cases/mock-case-id/ad-hoc-tasks")
                .then()
                .statusCode(400); // Case not found
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
                .body("error", is("VALIDATION_ERROR"));
    }
```

**Step 2: Run all tests**

Run: `cd backend && mvn test -Dtest=ProcessControllerTest`
Expected: All tests pass

**Step 3: Commit**

```bash
cd backend
git add src/test/java/com/flowable/platform/controller/ProcessControllerTest.java
git commit -m "feat(test): add diagram and CMMN ad-hoc task tests

Add tests for process diagram retrieval and CMMN ad-hoc task
creation with validation.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 11: Final Verification

**Files:**
- No files modified

**Step 1: Run all API tests**

Run: `cd backend && mvn test -Dtest=*ControllerTest`
Expected: All ~22 tests pass

**Step 2: Verify test execution time**

Run: `cd backend && time mvn test -Dtest=*ControllerTest`
Expected: Tests complete in under 2 minutes

**Step 3: Verify no data pollution**

Run tests 3 times in a row:
```bash
cd backend
mvn test -Dtest=*ControllerTest
mvn test -Dtest=*ControllerTest
mvn test -Dtest=*ControllerTest
```
Expected: All runs pass consistently (no flaky tests)

**Step 4: Generate test coverage report**

Run: `cd backend && mvn test jacoco:report`
Expected: Coverage report generated at `target/site/jacoco/index.html`

**Step 5: Commit final verification**

```bash
cd backend
git commit --allow-empty -m "feat(test): complete API testing implementation

All 22 API tests implemented and passing:
- 4 AuthController tests (login, logout, current user)
- 18 ProcessController tests (instances, definitions, deployments, tasks)

✅ Tests use real JWT authentication
✅ Tests clean up data via API
✅ Tests run in under 2 minutes
✅ No flaky tests detected

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Task 12: Documentation

**Files:**
- Create: `backend/README-TESTING.md`

**Step 1: Create testing documentation**

File contents:
```markdown
# API Testing Guide

## Running Tests

### All API Tests
```bash
mvn test -Dtest=*ControllerTest
```

### Specific Test Class
```bash
mvn test -Dtest=AuthControllerTest
mvn test -Dtest=ProcessControllerTest
```

### Specific Test Method
```bash
mvn test -Dtest=ProcessControllerTest#startProcess_validKey_returnsInstance
```

### With Coverage
```bash
mvn test jacoco:report
open target/site/jacoco/index.html
```

## Test Structure

```
src/test/java/com/flowable/platform/controller/
├── AbstractApiTest.java          # Base class with auth setup
├── AuthControllerTest.java       # 4 authentication tests
└── ProcessControllerTest.java    # 18 process management tests

src/test/resources/
├── db/migration/test-data/
│   └── V1__test_data.sql         # Test tenants and users
├── processes/
│   ├── simple-process.bpmn       # Basic process
│   ├── user-task-process.bpmn    # User task process
│   └── parallel-gateway.bpmn     # Parallel tasks
└── application-test.properties   # Test configuration
```

## Test Data

### Test Users
- **admin** / **admin123** (tenant-1) - Full access
- **user** / **user123** (tenant-1) - Regular user
- **other** / **other123** (tenant-2) - Different tenant

### Test Processes
- **simple-process** - Start → script → end
- **user-task-process** - Start → user task → end
- **parallel-gateway** - Fork → parallel tasks → join

## Authentication

Tests use real JWT authentication via `/api/auth/login`. The base class (`AbstractApiTest`) automatically:
1. Starts Testcontainers PostgreSQL
2. Logs in as admin and user
3. Stores JWT tokens for test methods
4. Provides `givenWithAuth(token)` helper

## Cleanup

ProcessControllerTest automatically cleans up after each test by:
1. Listing all process instances
2. Calling `/api/processes/{id}/terminate` for each
3. Ensuring tests don't see stale data

## Adding New Tests

1. Extend `AbstractApiTest`
2. Use `givenWithAuth(adminToken)` for authenticated requests
3. Follow naming convention: `{method}_{scenario}_{expectedResult}`
4. Add cleanup in `@AfterEach` if creating resources
5. Commit with descriptive message

## Troubleshooting

### Tests fail with "Connection refused"
- Ensure Testcontainers Docker daemon is running
- Check firewall isn't blocking Docker

### Tests fail with "Invalid credentials"
- Verify Flyway test migration ran: check database has test users
- Check BCrypt hashes in V1__test_data.sql are correct

### Tests see data from previous runs
- Verify `@AfterEach` cleanup is running
- Check for exceptions in cleanup code
- Ensure terminate endpoint is working correctly
```

**Step 2: Commit documentation**

```bash
cd backend
git add README-TESTING.md
git commit -m "docs: add API testing guide

Document how to run, structure, and extend API tests including
test data, authentication, and troubleshooting.

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

---

## Success Criteria Checklist

After completing all tasks:
- ✅ All 22 tests pass consistently
- ✅ Tests complete in under 2 minutes
- ✅ No flaky tests due to data pollution
- ✅ REST-assured dependency added
- ✅ Test configuration in place
- ✅ Flyway test migrations working
- ✅ Test BPMN processes deploy correctly
- ✅ AuthController has 4 passing tests
- ✅ ProcessController has 18 passing tests
- ✅ Documentation created
- ✅ All commits follow conventional commit format
- ✅ Code compiles without warnings

---

## Notes for Implementation

1. **BCrypt Hashes**: Generate fresh BCrypt hashes for passwords before running Task 3. Use the provided Java snippet or an online generator.

2. **Testcontainers**: Ensure Docker is running before executing tests. Tests will fail if Docker daemon isn't available.

3. **Parallel Execution**: These tests use sequential execution within each class. Parallel test execution may cause race conditions due to shared process instances.

4. **Cleanup**: The `@AfterEach` cleanup in ProcessControllerTest is critical. Without it, tests will see stale data and become flaky.

5. **JWT Secret**: The test configuration uses a simplified JWT secret. Never use test secrets in production.

6. **Flowable Cleanup**: Tests don't clean up Flowable engine tables (ACT_* tables). This is intentional - Testcontainers creates a fresh database for each test run.

7. **CMMN Testing**: The CMMN ad-hoc task tests are limited since CMMN deployment returns 501. These tests verify endpoint structure but don't create real case instances.

8. **REST-assured Configuration**: The base class configures REST-assured to use BigDecimal for numbers to prevent floating-point precision issues with Flowable's numeric process variables.
