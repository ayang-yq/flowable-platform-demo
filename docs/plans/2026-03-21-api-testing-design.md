# API Testing Design for Flowable Platform Demo

**Date:** 2026-03-21
**Author:** Claude Sonnet 4.6
**Status:** Approved

## Overview

Comprehensive functional API testing suite for all backend endpoints using REST-assured, Testcontainers, and real authentication flows.

## Architecture

### Test Framework Stack
- **REST-assured 5.4.0** - DSL-style API testing
- **Testcontainers** - PostgreSQL container for integration tests
- **JUnit 5** - Test runner (already configured)
- **Spring Boot Test** - Full application context testing

### Test Structure
```
src/test/java/com/flowable/platform/controller/
├── AbstractApiTest.java          (base class with shared setup)
├── AuthControllerTest.java       (~3-4 test methods)
└── ProcessControllerTest.java    (~15-20 test methods)

src/test/resources/
├── db/migration/test-data/
│   └── V1__test_data.sql         (Flyway test migrations)
├── processes/
│   ├── simple-process.bpmn       (basic process)
│   ├── user-task-process.bpmn    (for task testing)
│   └── parallel-gateway.bpmn     (for multi-task testing)
└── application-test.properties
```

## Authentication Setup

### AbstractApiTest Base Class
- Manages Testcontainers PostgreSQL lifecycle
- Performs real login via `/api/auth/login` in `@BeforeEach`
- Stores `adminToken` and `userToken` for different user roles
- Provides `givenWithAuth(String token)` helper method
- Configures REST-assured with random server port

### JWT Token Management
Tests use real authentication flow - no mocked JWT validation. Each test class gets pre-authenticated tokens for:
- **admin** - Full access permissions
- **user** - Regular user permissions

## Test Data Management

### Flyway Test Migrations
**Location:** `src/test/resources/db/migration/test-data/V1__test_data.sql`

**Data Populated:**
- 2 test tenants: `tenant-1`, `tenant-2`
- 3 test users with BCrypt password hashes:
  - `admin@tenant-1` (password: `admin123`)
  - `user@tenant-1` (password: `user123`)
  - `other@tenant-2` (password: `other123`)

### Test Process Definitions
**Location:** `src/test/resources/processes/`

1. **simple-process.bpmn** - Basic start → script task → end
2. **user-task-process.bpmn** - Start → user task → end
3. **parallel-gateway.bpmn** - Fork → parallel tasks → join → end

**Deployment Strategy:**
ProcessControllerTest deploys these via the `/api/processes/definitions` API endpoint in `@BeforeEach`, ensuring realistic integration testing.

## Test Coverage

### AuthController Tests (~4 tests)
1. ✅ `login_validCredentials_returnsToken` - Successful login returns JWT
2. ✅ `login_invalidCredentials_returnsError` - Wrong password returns error
3. ✅ `logout_success_returnsOk` - Logout endpoint works
4. ✅ `getCurrentUser_returnsUserInfo` - `/api/auth/me` returns user data

### ProcessController Tests (~18 tests)

**Process Instance Management:**
1. ✅ `startProcess_validKey_returnsInstance` - Create process instance
2. ✅ `startProcess_invalidKey_returnsError` - Non-existent process key
3. ✅ `listProcessInstances_returnsList` - Get all instances
4. ✅ `getProcessInstance_validId_returnsDetails` - Get single instance
5. ✅ `getProcessInstance_invalidId_returns404` - Invalid instance ID
6. ✅ `suspendProcessInstance_worksCorrectly` - Suspend instance
7. ✅ `activateProcessInstance_worksCorrectly` - Activate suspended instance
8. ✅ `terminateProcessInstance_deletesInstance` - Terminate instance
9. ✅ `getProcessTasks_returnsTaskList` - Get tasks for instance

**Process Definition Management:**
10. ✅ `deployProcessDefinition_validBpmnFile_success` - Deploy BPMN file
11. ✅ `deployProcessDefinition_emptyFile_returns400` - Empty file validation
12. ✅ `deployProcessDefinition_unsupportedFileType_returns400` - File type validation
13. ✅ `deployProcessDefinition_cmmnNotImplemented_returns501` - CMMN not supported
14. ✅ `listProcessDefinitions_returnsList` - Get all definitions
15. ✅ `getProcessDiagram_returnsSvg` - Get process diagram SVG

**CMMN Case Management:**
16. ✅ `createAdHocTask_validRequest_success` - Create ad-hoc task
17. ✅ `createAdHocTask_missingTaskName_returns400` - Validation error
18. ✅ `createAdHocTask_invalidCaseId_returns404` - Case not found

**Total:** ~22 comprehensive API tests

## Test Method Structure

### Naming Convention
`{methodName}_{scenario}_{expectedResult}`

Examples:
- `login_validCredentials_returnsToken`
- `getProcessInstance_invalidId_returns404`
- `deployProcessDefinition_emptyFile_returns400`

### Typical Test Structure
```java
@Test
void startProcess_validKey_returnsInstance() {
    // Arrange
    StartProcessRequest request = new StartProcessRequest("simple-process", Map.of());

    // Act & Assert
    givenWithAuth(adminToken)
        .body(request)
    .when()
        .post("/api/processes")
    .then()
        .statusCode(200)
        .body("data.processDefinitionKey", is("simple-process"))
        .body("data.id", notNullValue());
}
```

## Data Cleanup & Isolation

### Strategy: API-Based Cleanup
Tests use the `/api/processes/{id}/terminate` endpoint to clean up process instances in `@AfterEach`.

### ProcessControllerTest Cleanup
```java
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
    );
}
```

**Benefits:**
- Tests the terminate endpoint
- Ensures test isolation
- No stale data between test runs
- Reproducible failures

## Maven Configuration

### Additional Dependency
```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

### Test Configuration
**File:** `src/test/resources/application-test.properties`

```properties
# Testcontainers database
spring.datasource.url=jdbc:tc:postgresql:16:///testdb
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver

# Disable mail
spring.mail.host=localhost
spring.mail.port=25

# JWT configuration
jwt.secret=test-secret-key-for-jwt-token-generation-minimum-256-bits
jwt.expiration=3600000

# Logging
logging.level.com.flowable.platform=DEBUG
logging.level.org.flowable=WARN
```

### Running Tests
```bash
# All API tests
mvn test -Dtest=*ControllerTest

# Specific test class
mvn test -Dtest=AuthControllerTest

# With coverage
mvn test jacoco:report
```

## Edge Cases Covered

### Basic Error Scenarios
- Invalid credentials (401/500)
- Invalid resource IDs (404)
- Empty file uploads (400)
- Unsupported file types (400)
- Missing required fields (400)

### Business Logic Edge Cases
- Process state transitions (suspend/activate)
- Non-existent process instances
- Non-existent case instances
- File upload validation (BPMN vs CMMN vs DMN)

## What's Not Included (Deliberately)

**Multi-tenant Isolation Testing** - Can be added later as separate test suite
**Performance Testing** - Out of scope for functional API tests
**Security Penetration Testing** - Auth flow is tested, but not security exploits
**Flowable Engine Internals** - Focus is on API layer, not engine behavior

## Success Criteria

✅ All 22 tests pass consistently
✅ Tests complete in under 2 minutes
✅ No flaky tests due to data pollution
✅ CI/CD can run tests reliably
✅ New endpoints can be tested following established patterns

## Implementation Phases

1. **Setup** - Add dependencies, create base class, configure test properties
2. **Test Data** - Create Flyway migrations and BPMN files
3. **Auth Tests** - Implement AuthControllerTest
4. **Process Tests** - Implement ProcessControllerTest
5. **Verification** - Run all tests, verify coverage, add to CI/CD

---

**Next Action:** Proceed to implementation planning using superpowers:writing-plans skill.
