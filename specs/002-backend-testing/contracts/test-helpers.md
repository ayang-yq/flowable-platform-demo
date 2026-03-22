# Test Helper Interfaces and Base Classes

**Feature**: Backend Testing Infrastructure (002-backend-testing)
**Date**: 2026-03-22
**Status**: Complete

## Overview

This document defines the test helper interfaces, base test classes, and utility methods that provide common functionality for writing tests. These helpers reduce boilerplate code and ensure consistent testing patterns across the codebase.

## Base Test Classes

### 1. AbstractUnitTest

**Purpose**: Base class for unit tests with Mockito setup.

**Package**: `com.flowable.platform.test.unit`

**Features**:
- Mockito initialization (@ExtendWith(MockitoExtension.class))
- Common assertion methods
- Test data builders
- Mock creation helpers

**Code Structure**:

```java
package com.flowable.platform.test.unit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractUnitTest {

    /**
     * Create a mock of the specified class
     */
    protected <T> T mock(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

    /**
     * Create a spy of the specified class
     */
    protected <T> T spy(T instance) {
        return Mockito.spy(instance);
    }

    /**
     * Verify a mock was called exactly once
     */
    protected <T> void verifyCalledOnce(T mock) {
        verify(mock, times(1)).anyMethod();
    }

    /**
     * Verify a mock was never called
     */
    protected <T> void verifyNeverCalled(T mock) {
        verify(mock, never()).anyMethod();
    }
}
```

**Usage Example**:

```java
class UserServiceTest extends AbstractUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // Given
        User user = TestDataBuilder.aUser().build();
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.create(user);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(user);
    }
}
```

### 2. AbstractIntegrationTest

**Purpose**: Base class for integration tests with @SpringBootTest, Testcontainers, and REST Assured.

**Package**: `com.flowable.platform.test.integration`

**Features**:
- Spring Boot test context (@SpringBootTest)
- Testcontainers for PostgreSQL database
- Transactional rollback for test isolation
- Test tenant context setup
- REST Assured for API testing
- Request specification builder for common settings

**Code Structure**:

```java
package com.flowable.platform.test.integration;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.testcontainers.junit.jupiter.Testcontainers;
import static io.restassured.RestAssured.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Transactional
@Testcontainers
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestTenantContext testTenantContext;

    protected RequestSpecification requestSpec;

    @BeforeEach
    void setUp() {
        // Establish test tenant context
        testTenantContext.setTestTenant();

        // Configure REST Assured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Build default request specification
        requestSpec = new RequestSpecBuilder()
            .setBasePath("/api")
            .setContentType("application/json")
            .build();
    }

    /**
     * Get authenticated request specification
     */
    protected RequestSpecification authenticatedRequest(String username, String password) {
        return requestSpec.auth().preemptive().basic(username, password);
    }

    /**
     * Get authenticated request specification for test user
     */
    protected RequestSpecification authenticatedAsUser() {
        return authenticatedRequest("test-user", "test-user-password");
    }

    /**
     * Get authenticated request specification for test admin
     */
    protected RequestSpecification authenticatedAsAdmin() {
        return authenticatedRequest("test-admin", "test-admin-password");
    }
}
```

**Usage Example**:

```java
class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Test
    @DisplayName("Should get tasks for authenticated user")
    void shouldGetTasksForAuthenticatedUser() {
        // Given
        var request = authenticatedAsUser();

        // When
        var response = request
            .get("/tasks")
            .then()
            .statusCode(200)
            .extract()
            .response();

        // Then
        List<TaskDTO> tasks = response.jsonPath().getList("data", TaskDTO.class);
        assertThat(tasks).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 401 for unauthenticated request")
    void shouldReturn401ForUnauthenticatedRequest() {
        // When
        given()
            .spec(requestSpec)
        .when()
            .get("/tasks")
        .then()
            .statusCode(401);
    }
}
```

### 3. AbstractFlowableTest

**Purpose**: Base class for Flowable process and task testing.

**Package**: `com.flowable.platform.test.flowable`

**Features**:
- Flowable services autowired
- Test process deployment
- Task completion helpers
- Process instance verification
- History validation

**Code Structure**:

```java
package com.flowable.platform.test.flowable;

import org.springframework.beans.factory.annotation.Autowired;
import org.flowable.engine.*;

@SpringBootTest
@Transactional
public abstract class AbstractFlowableTest {

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected TaskService taskService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected IdentityService identityService;

    @Autowired
    protected TestTenantContext testTenantContext;

    @BeforeEach
    void setUp() {
        testTenantContext.setTestTenant();
    }

    /**
     * Deploy test process definition
     */
    protected String deployProcess(String processResource) {
        return repositoryService.createDeployment()
            .addClasspathResource("processes/" + processResource)
            .deploy()
            .getId();
    }

    /**
     * Start a process instance
     */
    protected ProcessInstance startProcess(String processDefinitionKey) {
        return runtimeService.startProcessInstanceByKey(processDefinitionKey);
    }

    /**
     * Complete a task by task ID
     */
    protected void completeTask(String taskId) {
        taskService.complete(taskId);
    }

    /**
     * Complete a task with variables
     */
    protected void completeTaskWithVariables(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * Assert process instance is completed
     */
    protected void assertProcessCompleted(String processInstanceId) {
        HistoricProcessInstance hpi = historyService
            .createHistoricProcessInstanceQuery()
            .processInstanceId(processInstanceId)
            .singleResult();

        assertThat(hpi.getEndActivityId()).isNotNull();
    }

    /**
     * Assert task exists for assignee
     */
    protected void assertTaskExists(String taskName, String assignee) {
        Task task = taskService.createTaskQuery()
            .taskName(taskName)
            .taskAssignee(assignee)
            .singleResult();

        assertThat(task).isNotNull();
    }
}
```

**Usage Example**:

```java
class ProcessIntegrationTest extends AbstractFlowableTest {

    private String deploymentId;

    @BeforeAll
    void setupProcesses() {
        deploymentId = deployProcess("test-simple-process.bpmn20.xml");
    }

    @AfterAll
    void cleanup() {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    @Test
    void shouldCompleteSimpleProcess() {
        // Given
        ProcessInstance pi = startProcess("test-simple-process");

        // When
        Task task = taskService.createTaskQuery()
            .processInstanceId(pi.getId())
            .singleResult();

        completeTask(task.getId());

        // Then
        assertProcessCompleted(pi.getId());
    }
}
```

## Test Configuration Classes

### TestConfig

**Purpose**: Main test configuration bean.

**Package**: `com.flowable.platform.test.config`

**Code**:

```java
package com.flowable.platform.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestConfiguration
public class TestConfig {

    @Bean
    public ObjectMapper testObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public FixtureLoader fixtureLoader(ObjectMapper objectMapper) {
        return new FixtureLoader(objectMapper);
    }
}
```

### TestTenantConfig

**Purpose**: Test tenant context management.

**Package**: `com.flowable.platform.test.config`

**Code**:

```java
package com.flowable.platform.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.UUID;

@TestConfiguration
public class TestTenantConfig {

    public static final UUID TEST_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Bean
    public TestTenantContext testTenantContext() {
        return new TestTenantContext(TEST_TENANT_ID);
    }
}
```

### WireMockConfig

**Purpose**: WireMock configuration for external HTTP service stubbing.

**Package**: `com.flowable.platform.test.config`

**Code**:

```java
package com.flowable.platform.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

@TestConfiguration
public class WireMockConfig {

    @Bean
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
            WireMockConfiguration.wireMockConfig().port(8089)
        );
        wireMockServer.start();
        return wireMockServer;
    }
}
```

## Test Utility Classes

### TestTenantContext

**Purpose**: Manages tenant context for tests.

**Package**: `com.flowable.platform.test.util`

**Code**:

```java
package com.flowable.platform.test.util;

import java.util.UUID;
import java.util.concurrent.Callable;

public class TestTenantContext {

    private final UUID testTenantId;
    private final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    public TestTenantContext(UUID testTenantId) {
        this.testTenantId = testTenantId;
    }

    /**
     * Set test tenant context for current thread
     */
    public void setTestTenant() {
        currentTenant.set(testTenantId);
    }

    /**
     * Set specific tenant context for current thread
     */
    public void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Get current tenant ID
     */
    public UUID getTenantId() {
        return currentTenant.get();
    }

    /**
     * Clear tenant context
     */
    public void clear() {
        currentTenant.remove();
    }

    /**
     * Execute callable with tenant context
     */
    public <T> T withTenantId(UUID tenantId, Callable<T> callable) throws Exception {
        try {
            setTenantId(tenantId);
            return callable.call();
        } finally {
            clear();
        }
    }
}
```

### FixtureLoader

**Purpose**: Loads test data fixtures from JSON files.

**Package**: `com.flowable.platform.test.util`

**Code**:

```java
package com.flowable.platform.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;

public class FixtureLoader {

    private final ObjectMapper objectMapper;

    public FixtureLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Load fixture from JSON file
     */
    public <T> T loadJson(String path, Class<T> clazz) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return objectMapper.readValue(is, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture: " + path, e);
        }
    }

    /**
     * Load multiple fixtures from JSON array file
     */
    public <T> List<T> loadJsonArray(String path, Class<T> clazz) {
        try {
            InputStream is = new ClassPathResource(path).getInputStream();
            return objectMapper.readValue(
                is,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fixture array: " + path, e);
        }
    }
}
```

### TestAuthenticationHelper

**Purpose**: Helper for creating authenticated REST Assured request specifications.

**Package**: `com.flowable.platform.test.util`

**Code**:

```java
package com.flowable.platform.test.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;

public class TestAuthenticationHelper {

    /**
     * Create request specification with basic auth
     */
    public static RequestSpecification withBasicAuth(String username, String password) {
        return new RequestSpecBuilder()
            .setContentType("application/json")
            .setAuth(preemptive().basic(username, password))
            .build();
    }

    /**
     * Create request specification with test admin credentials
     */
    public static RequestSpecification asAdmin() {
        return withBasicAuth("test-admin", "test-admin-password");
    }

    /**
     * Create request specification with test user credentials
     */
    public static RequestSpecification asUser() {
        return withBasicAuth("test-user", "test-user-password");
    }

    /**
     * Create request specification with test guest credentials
     */
    public static RequestSpecification asGuest() {
        return withBasicAuth("test-guest", "test-guest-password");
    }
}
```

## Test Assertions

### Custom AssertJ Assertions

**Purpose**: Custom assertions for domain objects.

**Package**: `com.flowable.platform.test.assertions`

**Example**:

```java
package com.flowable.platform.test.assertions;

import org.assertj.core.api.AbstractAssert;
import com.flowable.platform.entity.User;

public class UserAssert extends AbstractAssert<UserAssert, User> {

    public UserAssert(User actual) {
        super(actual, UserAssert.class);
    }

    public static UserAssert assertThat(User actual) {
        return new UserAssert(actual);
    }

    public UserAssert hasUsername(String username) {
        isNotNull();
        if (!actual.getUsername().equals(username)) {
            failWithMessage("Expected username to be <%s> but was <%s>", username, actual.getUsername());
        }
        return this;
    }

    public UserAssert hasRole(String role) {
        isNotNull();
        if (!actual.getRole().equals(role)) {
            failWithMessage("Expected role to be <%s> but was <%s>", role, actual.getRole());
        }
        return this;
    }

    public UserAssert isActive() {
        isNotNull();
        if (!actual.getIsActive()) {
            failWithMessage("Expected user to be active");
        }
        return this;
    }
}
```

**Usage**:

```java
@Test
void shouldCreateActiveUser() {
    User user = userService.create(testUser);
    UserAssert.assertThat(user)
        .isActive()
        .hasRole("USER")
        .hasUsername("test-user");
}
```

## Next Steps

With test helper interfaces defined, proceed to:
1. **quickstart.md**: Create developer guide for writing and running tests
2. Update agent context with testing framework information
