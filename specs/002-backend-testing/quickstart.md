# Quickstart Guide: Backend Testing

**Feature**: Backend Testing Infrastructure (002-backend-testing)
**Date**: 2026-03-22
**Audience**: Backend Developers

## Overview

This guide helps you quickly write and run tests for the Flowable platform backend. The testing infrastructure uses JUnit 5, Spring Boot Test, Mockito, and WireMock to provide comprehensive testing capabilities.

## Prerequisites

- Java 21 installed
- Maven or Gradle configured
- IDE with JUnit support (IntelliJ IDEA, Eclipse, VS Code)
- PostgreSQL running (for integration tests)

## Test Structure

```
backend/src/test/java/com/flowable/platform/
├── unit/                      # Unit tests (fast, no external dependencies)
│   ├── service/              # Service layer unit tests
│   ├── repository/           # Repository layer unit tests
│   └── util/                 # Utility class unit tests
├── integration/              # Integration tests (with database, embedded server)
│   ├── controller/           # API endpoint tests
│   ├── service/              # Service integration tests
│   └── workflow/             # Flowable process tests
└── config/                   # Test configuration
    ├── TestConfig.java
    ├── TestTenantConfig.java
    └── WireMockConfig.java
```

## Writing Tests

### 1. Unit Tests

Unit tests test business logic in isolation using Mockito for dependencies.

**Example**: Service Unit Test

```java
package com.flowable.platform.test.unit.service;

import com.flowable.platform.test.unit.AbstractUnitTest;
import com.flowable.platform.service.UserService;
import com.flowable.platform.repository.UserRepository;
import com.flowable.platform.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class UserServiceTest extends AbstractUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        // Given
        User user = TestDataBuilder.aUser()
            .withUsername("john.doe")
            .withEmail("john@example.com")
            .build();

        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.create(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john.doe");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should throw exception when username exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        User user = TestDataBuilder.aUser()
            .withUsername("existing.user")
            .build();

        when(userRepository.findByUsername("existing.user"))
            .thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> userService.create(user))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining("User with username 'existing.user' already exists");
    }
}
```

### 2. Integration Tests

Integration tests use @SpringBootTest with Testcontainers for PostgreSQL and REST Assured for API testing.

**Example**: Controller Integration Test with REST Assured

```java
package com.flowable.platform.test.integration.controller;

import com.flowable.platform.test.integration.AbstractIntegrationTest;
import com.flowable.platform.dto.TaskDTO;
import org.junit.jupiter.api.Test;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Should get tasks for authenticated user")
    void shouldGetTasksForAuthenticatedUser() {
        // Given
        var request = authenticatedAsUser();

        // When & Then
        request
            .get("/tasks")
            .then()
            .statusCode(200)
            .body("data", not(empty()))
            .body("data[0].taskId", notNullValue())
            .body("data[0].taskName", notNullValue());
    }

    @Test
    @DisplayName("Should return 401 for unauthenticated request")
    void shouldReturn401ForUnauthenticatedRequest() {
        // When & Then
        given()
            .spec(requestSpec)
        .when()
            .get("/tasks")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Should create task with POST request")
    void shouldCreateTask() {
        // Given
        String newTaskJson = """
            {
                "taskName": "Test Task",
                "description": "Test task description"
            }
            """;

        var request = authenticatedAsAdmin();

        // When & Then
        request
            .body(newTaskJson)
            .post("/tasks")
            .then()
            .statusCode(201)
            .body("taskId", notNullValue())
            .body("taskName", equalTo("Test Task"));
    }
}
```

### 3. Flowable Process Tests

Flowable tests extend AbstractFlowableTest for process and task testing.

**Example**: Process Integration Test

```java
package com.flowable.platform.test.integration.workflow;

import com.flowable.platform.test.flowable.AbstractFlowableTest;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class SimpleProcessTest extends AbstractFlowableTest {

    private String deploymentId;

    @BeforeAll
    void setupProcesses() {
        deploymentId = deployProcess("test-simple-process.bpmn20.xml");
    }

    @AfterAll
    void cleanup() {
        if (deploymentId != null) {
            repositoryService.deleteDeployment(deploymentId, true);
        }
    }

    @Test
    @DisplayName("Should complete simple process")
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

    @Test
    @DisplayName("Should complete task with variables")
    void shouldCompleteTaskWithVariables() {
        // Given
        ProcessInstance pi = startProcess("test-simple-process");
        Task task = taskService.createTaskQuery()
            .processInstanceId(pi.getId())
            .singleResult();

        Map<String, Object> variables = Map.of(
            "approved", true,
            "comment", "Test approval"
        );

        // When
        completeTaskWithVariables(task.getId(), variables);

        // Then
        assertProcessCompleted(pi.getId());

        Map<String, Object> processVars = runtimeService.getVariables(pi.getId());
        assertThat(processVars).containsEntry("approved", true);
    }
}
```

## Running Tests

### Run All Tests

```bash
# Maven
mvn test

# Gradle
./gradlew test
```

### Run Specific Test Class

```bash
# Maven
mvn test -Dtest=UserServiceTest

# Gradle
./gradlew test --tests UserServiceTest
```

### Run Specific Test Method

```bash
# Maven
mvn test -Dtest=UserServiceTest#shouldCreateUser

# Gradle
./gradlew test --tests UserServiceTest.shouldCreateUser
```

### Run Only Unit Tests

```bash
# Maven
mvn test -Dtest="com.flowable.platform.test.unit.**"

# Gradle
./gradlew test --tests "com.flowable.platform.test.unit.*"
```

### Run Only Integration Tests

```bash
# Maven
mvn test -Dtest="com.flowable.platform.test.integration.**"

# Gradle
./gradlew test --tests "com.flowable.platform.test.integration.*"
```

## Test Best Practices

### 1. Use Descriptive Test Names

```java
// Good
@Test
@DisplayName("Should return 404 when user not found")
void shouldReturn404WhenUserNotFound() { }

// Bad
@Test
void test1() { }
```

### 2. Follow Given-When-Then Pattern

```java
@Test
void shouldCreateUser() {
    // Given - setup test data
    User user = TestDataBuilder.aUser().build();

    // When - execute system under test
    User result = userService.create(user);

    // Then - verify results
    assertThat(result).isNotNull();
}
```

### 3. Use Test Data Builders

```java
// Good - uses builder pattern
User user = TestDataBuilder.aUser()
    .withUsername("john.doe")
    .withRole("ADMIN")
    .build();

// Bad - manual construction
User user = new User();
user.setUserId(UUID.randomUUID());
user.setUsername("john.doe");
user.setRole("ADMIN");
// ... tedious and error-prone
```

### 4. Avoid Over-Mocking

```java
// Good - mock only external dependencies
@Mock
private UserRepository userRepository;

@Mock
private EmailService emailService;  // external service

// Bad - mock system under test
@Mock
private UserService userService;  // DON'T MOCK THE CLASS UNDER TEST!
```

### 5. Test Behavior, Not Implementation

```java
// Good - tests behavior
@Test
void shouldCreateUserAndSendEmail() {
    userService.create(user);
    verify(emailService).sendWelcomeEmail(user.getEmail());
}

// Bad - tests implementation details
@Test
void shouldCallRepositorySaveMethod() {
    userService.create(user);
    verify(userRepository).save(any(User.class));  // implementation detail
}
```

### 6. Use @DisplayName for Documentation

```java
@Test
@DisplayName("""
    Should complete task with variables
    and update process instance variables
    """)
void shouldCompleteTaskWithVariables() {
    // This test documents what it does
}
```

## Common Test Scenarios

### Testing Exception Handling

```java
@Test
void shouldThrowExceptionWhenUserNotFound() {
    // Given
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When/Then
    assertThatThrownBy(() -> userService.getUser(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found: " + userId);
}
```

### Testing Multi-Tenancy

```java
@Test
void shouldIsolateDataByTenant() {
    // Given
    UUID tenant1 = TestTenantConfig.TEST_TENANT_ID;
    UUID tenant2 = UUID.randomUUID();

    TestDataBuilder.aUser().withTenantId(tenant1).build();
    TestDataBuilder.aUser().withTenantId(tenant2).build();

    // When - tenant1 context
    testTenantContext.setTenantId(tenant1);
    List<User> tenant1Users = userRepository.findAll();

    // Then - should only see tenant1 data
    assertThat(tenant1Users).hasSize(1);
    assertThat(tenant1Users.get(0).getTenantId()).isEqualTo(tenant1);
}
```

### Testing External HTTP Calls with WireMock

```java
@Test
void shouldCallExternalService() {
    // Given
    wireMockServer.stubFor(post(urlEqualTo("/api/external"))
        .willReturn(aResponse()
            .withStatus(200)
            .withBody("{\"status\":\"ok\"}")));

    // When
    ExternalServiceResponse response = externalService.call();

    // Then
    assertThat(response.getStatus()).isEqualTo("ok");
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/external")));
}
```

## Test Coverage

### Check Coverage Report

```bash
# Maven with JaCoCo
mvn test jacoco:report
open target/site/jacoco/index.html

# Gradle
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Coverage Targets (per constitution §Dev Workflow)

- **Service Layer**: 80% minimum (enforced by JaCoCo)
- **Controllers**: All public endpoints with happy path + error path
- **Utilities**: 60% minimum (enforced by JaCoCo)

## Debugging Tests

### IDE Debugging

1. Set breakpoint in test code
2. Right-click test method → Debug
3. Use IDE debugger to inspect variables

### Logging in Tests

```java
@Test
@DisplayName("Debug test")
@EnabledIfSystemProperty(named = "debug", matches = "true")
void debugTest() {
    log.debug("Starting test with debug logging");
    // Test logic here
}
```

Run with debug enabled:
```bash
mvn test -Ddebug=true
```

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: testdb
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run tests
        run: mvn test
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/testdb
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test
```

## Troubleshooting

### Test Fails with "Connection Refused"

**Problem**: Can't connect to database
**Solution**:
- Ensure Docker is running (Testcontainers requires Docker)
- Check Docker daemon is accessible
- Verify PostgreSQL container is starting

### Test Fails with "Tenant Context Not Set"

**Problem**: Tenant context not established
**Solution**: Ensure test extends AbstractIntegrationTest or calls `testTenantContext.setTestTenant()`

### Test Fails with "No qualifying bean of type"

**Problem**: Spring Boot can't find bean
**Solution**: Ensure test class has @SpringBootTest or appropriate slice annotation

### Tests Are Slow

**Problem**: Tests take too long
**Solution**:
- Use TC_GET_CONFIGURABLE_CONTAINERS=true to reuse Testcontainers
- Use @WebMvcTest instead of @SpringBootTest for controller-only tests
- Use @DataJpaTest for repository-only tests
- Avoid starting full application context unless needed

### REST Assured Tests Fail with "Port already in use"

**Problem**: Port conflict
**Solution**: Ensure @SpringBootTest uses WebEnvironment.RANDOM_PORT (not DEFINED_PORT)

### REST Assured Path Not Matching

**Problem**: Request returns 404 unexpectedly
**Solution**:
- Check base path in RequestSpecBuilder (should be "/api")
- Verify endpoint mapping in controller
- Enable request logging: `.log().all()` in REST Assured

### Testcontainers Fails to Start

**Problem**: Docker container fails to start
**Solution**:
- Ensure Docker Desktop is running
- Check Docker daemon logs
- Verify sufficient disk space and memory
- Try: `docker system prune -f`

## CI/CD Integration Details

### GitHub Actions Workflow

The project uses `.github/workflows/backend-test.yml` which runs:
1. **Unit tests**: `mvn test -Dtest="com.flowable.platform.test.unit.**"`
2. **Integration tests**: `mvn verify -Dtest="com.flowable.platform.test.integration.**"`
3. **Coverage report**: JaCoCo report generated and uploaded as artifact
4. **Build**: Maven package (only if tests pass)
5. **Security scan**: SpotBugs + OWASP dependency-check

### Local vs CI Differences

| Aspect | Local | CI/CD |
|--------|-------|-------|
| Database | Testcontainers (Docker) | PostgreSQL service container |
| Docker | Docker Desktop required | GitHub Actions Docker |
| Testcontainers Ryuk | Enabled (cleanup) | Disabled (`TESTCONTAINERS_RYUK_DISABLED=true`) |
| Coverage | Optional (`mvn verify jacoco:report`) | Automatic with artifact upload |

### Branch Protection Rules Setup

To enforce tests before merge (GitHub → Settings → Branches → Branch protection rules):

1. Select branch: `main` (or `develop`)
2. Enable "Require status checks to pass before merging"
3. Add required checks: `Backend Tests / Backend Tests`
4. Enable "Require branches to be up to date before merging"
5. Optionally enable "Require pull request reviews before merging"

## Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [REST Assured Documentation](https://rest-assured.io/)
- [REST Assured Usage Guide](https://github.com/rest-assured/rest-assured/wiki/Usage)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Testcontainers PostgreSQL Module](https://www.testcontainers.org/modules/databases/postgres/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [WireMock Documentation](http://wiremock.org/docs/)
- [AssertJ Assertions](https://assertj.github.io/doc/)

## Support

For questions or issues with testing infrastructure:
1. Check this quickstart guide
2. Review test examples in `backend/src/test/java/`
3. Consult [research.md](./research.md) for detailed technology decisions
4. Ask in team chat or create issue

---

**Last Updated**: 2026-03-22 | **Version**: 1.0.0
