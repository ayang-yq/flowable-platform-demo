# Implementation Plan: Backend Testing Infrastructure

**Branch**: `002-backend-testing` | **Date**: 2026-03-22 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-backend-testing/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Establish comprehensive testing infrastructure for the Flowable platform backend including unit tests for business logic isolation and API tests with real HTTP calls. Technical approach combines JUnit 5 with @SpringBootTest for embedded server integration testing, REST Assured for fluent API testing, Testcontainers for PostgreSQL database integration, Mockito for service layer mocking, WireMock for external HTTP service stubbing, and transactional rollback for test data management. Testing infrastructure supports multi-tenant isolation with dedicated test tenant context and enforces fail-fast reliability without retries to maintain test integrity.

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.5.x, Flowable 7.x, JUnit 5, Mockito 5.x, REST Assured 5.x, Testcontainers 1.19.x, WireMock 3.x, Spring Test, Spring Boot Test
**Storage**: PostgreSQL 15+ (multi-tenant schema strategy) via Testcontainers for integration tests
**Testing Framework**: JUnit 5 with Spring Boot Test, @SpringBootTest for integration tests, REST Assured for API testing, Mockito for unit tests, WireMock for external HTTP services, Testcontainers for database integration
**Test Data Management**: @Transactional with rollback for test isolation
**Test Server**: Embedded Tomcat via @SpringBootTest with random port
**API Testing**: REST Assured for fluent HTTP testing with given-when-then syntax
**Database Testing**: Testcontainers for PostgreSQL integration tests with real database
**Mocking Strategy**: Layered approach - Mockito for service layer dependencies in unit tests, WireMock for external HTTP service stubbing in API tests
**Multi-Tenant Testing**: Dedicated test tenant with @BeforeEach context switching
**Target Platform**: Linux server (backend tests)
**Project Type**: Web service (multi-tenant workflow platform)
**Performance Goals**: Unit tests complete in < 5 minutes total, API tests complete in < 10 minutes total, full test suite < 15 minutes
**Constraints**: Multi-tenant data isolation, Flowable-native services, transactional test boundaries, fail-fast test reliability
**Scale/Scope**: Multi-tenant SaaS testing infrastructure supporting 80% code coverage for service layer per constitution

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Multi-Tenant Isolation**: Does design maintain absolute tenant data separation?
  - **YES**: Dedicated test tenant with @BeforeEach context switching ensures tenant isolation in API tests. Transactional rollback prevents cross-test data leakage.
- [x] **Flowable-Native**: Are we using TaskService/RuntimeService/HistoryService directly (no abstractions)?
  - **YES**: Tests will use real Flowable services (TaskService, RuntimeService, HistoryService) in integration tests, not mocked abstractions.
- [ ] **Server/Client Boundaries**: Are all data fetching and Flowable calls in Server Components only?
  - **N/A**: This is a backend-only testing infrastructure feature. Frontend boundaries are not in scope.
- [ ] **Form-Process Binding**: Do SurveyJS forms map correctly to Flowable process variables?
  - **N/A**: Form testing is not in scope for this backend testing infrastructure feature.
- [x] **Test Coverage**: Are there integration tests for all workflow gateways and decision points?
  - **PARTIAL**: This feature establishes the testing infrastructure. Coverage targets (70% business logic, all public endpoints) are defined but actual test implementation comes later.
- [ ] **Audit Trail**: Is every workflow action logged with userId, tenantId, timestamp, IP?
  - **N/A**: Audit trail testing is part of the feature being tested, not the testing infrastructure itself.
- [x] **Performance**: Do database queries include tenant_id filtering?
  - **YES**: API tests validate that tenant_id filtering works correctly by using dedicated test tenant and checking data isolation.
- [ ] **Security**: Are process variables encrypted for sensitive data?
  - **N/A**: Security testing is out of scope (excluded in spec - "Security penetration testing (specialized tools and expertise required)").

**Overall Assessment**: ✅ **PASS** - All applicable constitution principles are addressed. Testing infrastructure will validate multi-tenant isolation, Flowable-native integration, and performance requirements through comprehensive test coverage.

## Project Structure

### Documentation (this feature)

```text
specs/002-backend-testing/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── test/
│   │   ├── java/com/flowable/platform/
│   │   │   ├── unit/                      # Unit tests (Mockito, no external dependencies)
│   │   │   │   ├── service/               # Service layer unit tests
│   │   │   │   ├── repository/            # Repository layer unit tests
│   │   │   │   └── util/                  # Utility class unit tests
│   │   │   ├── integration/              # Integration tests (@SpringBootTest, embedded server)
│   │   │   │   ├── controller/           # API endpoint tests (real HTTP calls)
│   │   │   │   ├── service/              # Service integration tests (with database)
│   │   │   │   └── workflow/             # Flowable process integration tests
│   │   │   ├── config/                   # Test configuration
│   │   │   │   ├── TestConfig.java       # Main test configuration
│   │   │   │   ├── TestTenantConfig.java # Test tenant context setup
│   │   │   │   └── WireMockConfig.java   # WireMock configuration for external services
│   │   │   └── resources/
│   │   │       ├── application-test.yml  # Test configuration
│   │   │       ├── db/migration/         # Test database migrations (including test tenant)
│   │   │       └── fixtures/             # Test data fixtures
│   │   └── ...
│   └── ...
└── pom.xml                                   # Updated with test dependencies
```

**Structure Decision**: Backend testing infrastructure follows Spring Boot standard layout with separated unit and integration test directories. Unit tests (`src/test/java/unit/`) use Mockito for fast isolation testing. Integration tests (`src/test/java/integration/`) use @SpringBootTest with embedded server and real HTTP client for comprehensive API testing. Test configuration is centralized in `config/` package with dedicated tenant context management. Test resources include application-test.yml for environment-specific configuration and fixture files for test data.

### Test Dependencies (pom.xml additions)

```xml
<dependencies>
    <!-- Testing Framework -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- REST Assured for API Testing -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>

    <!-- REST Assured JSON Path support -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>json-path</artifactId>
        <version>5.4.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers for PostgreSQL -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers JUnit 5 integration -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>1.19.3</version>
        <scope>test</scope>
    </dependency>

    <!-- WireMock for External HTTP Services -->
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>3.5.2</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito for Unit Tests (included in spring-boot-starter-test) -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-inline</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

No constitution violations requiring justification. Testing infrastructure aligns with all applicable constitutional principles.
