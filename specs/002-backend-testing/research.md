# Research: Backend Testing Infrastructure

**Feature**: Backend Testing Infrastructure (002-backend-testing)
**Date**: 2026-03-22
**Status**: Complete

## Overview

This document captures research findings for establishing comprehensive testing infrastructure for the Flowable platform backend. Research focused on testing frameworks, mock strategies, database test management, multi-tenant testing patterns, and CI/CD integration for Spring Boot applications.

## Technology Decisions

### 1. Testing Framework: JUnit 5 + Spring Boot Test

**Decision**: Use JUnit 5 (Jupiter) as the primary testing framework with Spring Boot Test for integration testing.

**Rationale**:
- JUnit 5 is the standard testing framework for Java applications with modern features (parameterized tests, nested tests, dynamic tests)
- Spring Boot Test provides @SpringBootTest for full application context testing with embedded server
- Native Spring integration through @TestConfiguration, @MockBean, and test profiles
- Excellent IDE support and seamless Maven/Gradle integration
- Large community and extensive documentation

**Alternatives Considered**:
- **TestNG**: Powerful but less popular in Spring Boot ecosystem, fewer modern features
- **Spock**: Groovy-based, expressive syntax but introduces language complexity for Java teams

**Best Practices**:
- Use @SpringBootTest for integration tests requiring full application context
- Use @WebMvcTest for controller-only tests (faster than full context)
- Use @DataJpaTest for repository-only tests
- Use @DisplayName annotations for descriptive test names
- Use @Nested for grouping related tests
- Use @ParameterizedTest for data-driven testing

### 2. API Testing Framework: REST Assured

**Decision**: Use REST Assured for API integration testing with fluent given-when-then syntax.

**Rationale**:
- **Fluent DSL**: REST Assured provides intuitive given-when-then syntax that reads like natural language
- **JSON Path Support**: Built-in JSON path extraction and validation without additional libraries
- **Spring Integration**: Seamless integration with Spring Boot Test via @LocalServerPort
- **Comprehensive**: Supports all HTTP methods, headers, cookies, authentication, and complex assertions
- **Readable**: Test code is self-documenting and easy to understand
- **Mature**: Widely adopted in the Java ecosystem with excellent documentation

**Alternatives Considered**:
- **TestRestTemplate**: Spring's built-in REST client - requires more verbose code and less expressive assertions
- **WebTestClient**: Reactive alternative - different paradigm, steeper learning curve
- **Postman/Newman**: External tool - requires separate test files, less integrated with Java code

**Best Practices**:
- Use static imports for REST Assured DSL (given, when, then, assertThat)
- Use @LocalServerPort to inject random port from @SpringBootTest
- Create base test class with common setup (authentication, base URL)
- Use JSON path for precise field validation (e.g., `response.path("data[0].name")`)
- Use filters for logging requests/responses during test development
- Group related tests using @Nested for better organization
- Use RequestSpecBuilder for common request specifications (authentication, content type)

### 3. Mocking Strategy: Mockito (Unit Tests) + WireMock (API Tests)

**Decision**: Layered mocking approach - Mockito for service layer dependencies in unit tests, WireMock for external HTTP services in API tests.

**Rationale**:
- **Mockito**: Industry standard for Java mocking, clean API, powerful verification capabilities, excellent Spring Boot integration via @MockBean
- **WireMock**: HTTP-specific mocking, simulates real external service behavior including latency and failures, supports request matching and response stubbing
- Layered approach provides comprehensive coverage: Mockito isolates business logic, WireMock validates HTTP client behavior
- Both frameworks integrate seamlessly with Spring Boot Test

**Alternatives Considered**:
- **Mockito everywhere**: Simple but doesn't test actual HTTP client behavior in API tests
- **No mocking in API tests**: Most realistic but slow and unreliable (depends on external service availability)
- **In-memory fakes**: Fast but requires maintenance and may diverge from real services

**Best Practices**:
- Use @MockBean for mocking Spring beans in tests
- Use @SpyBean for partial mocking (real implementation with selective stubbing)
- Use Mockito.verify() to validate interactions
- Use WireMock's stubFor() to define HTTP response stubs
- Reset mocks between tests using @DirtiesContext or Mockito.reset()
- Avoid over-mocking - only mock external dependencies, not the system under test

### 4. Database Testing: Testcontainers + Transactional Rollback

**Decision**: Use Testcontainers for PostgreSQL database integration with @Transactional for automatic rollback.

**Rationale**:
- **Testcontainers**: Provides real PostgreSQL database in Docker containers - tests run against actual database, not H2 in-memory
- **Production Parity**: Tests run against same database as production, catching schema and query issues early
- **Isolated**: Each test gets fresh database container, no shared state between tests
- **Automatic**: Testcontainers manages container lifecycle (start/stop) automatically via JUnit 5 integration
- **Transactional Rollback**: @Transactional annotation provides fast, reliable test data cleanup
- **Multi-Schema Support**: Testcontainers supports multiple schemas for multi-tenant testing

**Alternatives Considered**:
- **In-memory H2 database**: Faster startup but requires schema synchronization, may diverge from PostgreSQL behavior
- **Shared test database**: Requires careful data management, tests can interfere with each other
- **Database cleanup scripts**: Explicit CREATE/DELETE statements - slower, more brittle, but mirrors production patterns

**Best Practices**:
- Use @Testcontainers and @Container annotations for automatic container lifecycle
- Use PostgreSQLContainer from Testcontainers for database setup
- Use @Transactional on test classes for automatic rollback after each test
- Use @BeforeEach for test-specific data setup within transaction
- Use fixtures in src/test/resources/fixtures/ for reusable test data
- Use Flyway migrations to create test tenant schema
- Validate transaction boundaries in tests (no premature commits)
- Use TC_GET_CONFIGURABLE_CONTAINERS environment variable to reuse containers for faster local development

### 5. Multi-Tenant Testing: Dedicated Test Tenant with Context Switching

**Decision**: Create a dedicated test tenant via migration scripts and use @BeforeEach to establish tenant context before each test.

**Rationale**:
- Fast - single test tenant reused across all tests
- Pragmatic - validates multi-tenant behavior without per-test tenant overhead
- Clear - tenant context is explicitly established in test setup
- Maintains tenant isolation through transactional rollback
- Enables validation of cross-tenant data leakage prevention

**Alternatives Considered**:
- **Per-test tenant creation**: Maximum isolation but slower due to tenant setup overhead
- **Mock tenant context**: Faster but doesn't validate actual multi-tenant isolation
- **Ignore multi-tenancy in tests**: Simpler but risks missing multi-tenant bugs

**Best Practices**:
- Create test tenant in V__test_tenant.sql migration script
- Use @BeforeEach to set tenant context via ThreadLocal or similar mechanism
- Validate tenant_id in test assertions
- Test cross-tenant data access attempts (should fail)
- Use tenant-aware test data fixtures

### 6. API Test Server: Embedded Spring Boot Server with REST Assured

**Decision**: Use @SpringBootTest with embedded Tomcat server and REST Assured for API integration testing.

**Rationale**:
- Realistic integration - full HTTP stack including servlet filters, serialization, routing
- Standard Spring Boot approach - @SpringBootTest starts embedded server automatically
- Comprehensive - tests controllers, services, repositories together
- HTTP-level validation - status codes, headers, response body, content types
- REST Assured DSL - fluent given-when-then syntax for readable test code
- Fast enough for CI/CD - embedded server startup is ~2-3 seconds

**Alternatives Considered**:
- **MockMvc**: Tests Spring MVC layer without HTTP stack - faster but less realistic
- **TestRestTemplate**: More verbose code, less expressive assertions
- **WebTestClient**: Reactive alternative - different paradigm, steeper learning curve

**Best Practices**:
- Use @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) for HTTP testing
- Use @LocalServerPort to inject random port for REST Assured base URI
- Create RequestSpecBuilder with common settings (authentication, content type)
- Use REST Assured JSON path for precise field validation
- Use filters for request/response logging during test development
- Group API tests by controller or endpoint for organization
- Use @BeforeEach to reset request specifications if needed

### 7. Test Reliability: Fail-Fast with Explicit Flaky Test Annotation

**Decision**: Tests fail immediately without retries; known flaky tests can be annotated for tracking.

**Rationale**:
- Maintains test integrity - no hidden flakiness
- Forces prompt fixes - flaky tests break the build immediately
- Simple - no complex retry logic or quarantine mechanisms
- Transparent - flaky test visibility through annotations
- Aligns with "fail fast" principle for CI/CD

**Alternatives Considered**:
- **Automatic retry with test report**: Hides flakiness but unblocks developers
- **Quarantine flaky tests automatically**: Reduces noise but may hide legitimate issues
- **Ignore flaky test detection**: Simplest but may frustrate team

**Best Practices**:
- Use @Disabled annotation for temporarily disabled tests with reason
- Create custom @Flaky annotation for tracking intermittent failures
- Fail immediately on assertion errors or exceptions
- Log detailed error messages for debugging
- Avoid Thread.sleep() or time-based assertions - use explicit waits or condition awaiters
- Use @DirtiesContext to reset application state between tests if needed

### 8. Test Configuration: application-test.yml

**Decision**: Use Spring's test profile configuration (application-test.yml) for test-specific settings.

**Rationale**:
- Standard Spring Boot approach - active profile "test" in test environment
- Separates test configuration from production configuration
- Enables test database, test tenant, and external service stubs
- Supports environment-specific test configurations (test, integration, local)
- Easy to maintain alongside application.yml

**Best Practices**:
- Configure test database connection (PostgreSQL test database)
- Set logging levels for test output (DEBUG for packages under test)
- Configure test tenant ID and credentials
- Configure external service stub URLs (WireMock endpoints)
- Disable or mock external services that shouldn't run in tests
- Use @ActiveProfiles("test") to activate test profile

## Integration Patterns

### Spring Boot Test Annotations

| Annotation | Purpose | Scope |
|------------|---------|-------|
| @SpringBootTest | Full application context with embedded server | Integration tests |
| @WebMvcTest | Spring MVC layer only (controllers) | Controller unit tests |
| @DataJpaTest | JPA repositories only | Repository unit tests |
| @JsonTest | JSON serialization/deserialization | JSON binding tests |
| @MockBean | Mock Spring bean | Any test |
| @SpyBean | Partial mock of Spring bean | Any test |
| @Transactional | Transactional test with rollback | Integration tests |
| @DirtiesContext | Reset application context | State cleanup |
| @BeforeAll/@BeforeEach | Setup methods | Test lifecycle |
| @ParameterizedTest | Data-driven tests | Parameterized tests |

### Test Layering Strategy

```
┌─────────────────────────────────────────┐
│  E2E Tests (future - out of scope)      │  Full user journeys
├─────────────────────────────────────────┤
│  API Tests (@SpringBootTest)            │  HTTP integration
│  - REST Assured HTTP calls              │  Controllers + Services
│  - Embedded server (RANDOM_PORT)        │  + Repositories + DB
│  - Testcontainers PostgreSQL            │
│  - WireMock for external services       │
├─────────────────────────────────────────┤
│  Service Integration Tests              │  Database integration
│  - @DataJpaTest                         │  Services + Repositories
│  - Testcontainers PostgreSQL            │
├─────────────────────────────────────────┤
│  Unit Tests (Mockito)                   │  Business logic
│  - Mock dependencies                    │  Single class/method
│  - Fast execution                       │
└─────────────────────────────────────────┘
```

## Performance Considerations

### Test Execution Time Targets

- **Unit tests**: < 5 minutes total (target: ~100-200 tests, averaging 1-2 seconds each)
- **API tests**: < 10 minutes total (target: ~50-100 tests, averaging 5-10 seconds each)
- **Full test suite**: < 15 minutes total (parallel execution in CI/CD)

### Optimization Strategies

1. **Use @SpringBootTest judiciously**: Only for true integration tests
2. **Use slice tests** (@WebMvcTest, @DataJpaTest) for faster feedback
3. **Group tests by granularity**: Run unit tests first, then integration tests
4. **Parallel execution**: Use JUnit 5's parallel execution in CI/CD
5. **Test containers**: Consider Testcontainers for database tests (slower but more realistic)
6. **Embedded server startup**: Reuse application context within test class

## Dependencies

### Maven Dependencies

```xml
<!-- Spring Boot Test (includes JUnit 5, Mockito, Spring Test) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- REST Assured for API testing -->
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

<!-- WireMock for HTTP service stubbing -->
<dependency>
    <groupId>org.wiremock</groupId>
    <artifactId>wiremock-standalone</artifactId>
    <version>3.5.2</version>
    <scope>test</scope>
</dependency>
```

### Version Alignment

- **Spring Boot**: 3.5.x (aligns with main application)
- **JUnit**: 5.10.x (included in spring-boot-starter-test)
- **Mockito**: 5.x (included in spring-boot-starter-test)
- **REST Assured**: 5.4.x (explicit dependency)
- **WireMock**: 3.5.x (explicit dependency)
- **Testcontainers**: 1.19.x (explicit dependency)

## Open Questions Resolved

### Q: How to handle authentication in API tests?
**A**: Use REST Assured authentication with preemptive basic auth or test JWT tokens. Configure auth in RequestSpecBuilder for reuse across tests.

### Q: How to handle Flowable engine in tests?
**A**: Use @SpringBootTest to load Flowable configuration. Use TestTenantConfig to set tenant context before Flowable operations. Use FlowableTestHelper for common test utilities (start process, complete task, verify state).

### Q: How to organize test data fixtures?
**A**: Create fixture files in src/test/resources/fixtures/ as JSON or SQL. Use @Sql annotation or FixtureLoader utility to load fixtures in tests. Group fixtures by entity or test scenario.

### Q: How to handle test failures in CI/CD?
**A**: Configure CI/CD to fail pipeline on test failures. Generate test reports (Surefire, Failsafe). Archive test logs for debugging. Notify developers via email/Slack on failures.

## References

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [WireMock Documentation](http://wiremock.org/docs/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Test Annotations](https://spring.io/guides/gs/testing-web/)

## Next Steps

Proceed to Phase 1 (Design & Contracts) to create:
1. **data-model.md**: Define test entity structures and test data model
2. **contracts/**: Document test interfaces and helper classes
3. **quickstart.md**: Developer guide for writing and running tests
