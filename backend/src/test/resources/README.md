# Backend Testing Guide

## Overview

This project uses a layered testing approach:

| Layer | Base Class | Tools | Speed |
|-------|-----------|-------|-------|
| Unit | `AbstractUnitTest` | Mockito, AssertJ | Fast (~ms) |
| Integration | `AbstractIntegrationTest` | REST Assured, Testcontainers | Medium (~s) |
| Flowable | `AbstractFlowableTest` | Flowable services, Testcontainers | Medium (~s) |

## Writing a New Unit Test

1. Create class in `test/unit/service/`, `test/unit/repository/`, or `test/unit/util/`
2. Extend `AbstractUnitTest`
3. Use `@Mock` for dependencies, `@InjectMocks` for the class under test
4. Follow Given-When-Then pattern

```java
class MyServiceTest extends AbstractUnitTest {
    @Mock private MyRepository repository;
    @InjectMocks private MyService service;

    @Test
    @DisplayName("Should do something")
    void shouldDoSomething() {
        // Given
        when(repository.findById(any())).thenReturn(Optional.of(entity));
        // When
        var result = service.doSomething(id);
        // Then
        assertThat(result).isNotNull();
    }
}
```

## Writing a New API Test

1. Create class in `test/integration/controller/`
2. Extend `AbstractIntegrationTest`
3. Use REST Assured `given().when().then()` pattern

```java
class MyControllerTest extends AbstractIntegrationTest {
    @Test
    void shouldGetResource() {
        givenAsUser()
            .get("/my-resource")
        .then()
            .statusCode(200)
            .body("data", notNullValue());
    }
}
```

## Writing a Flowable Process Test

1. Create class in `test/integration/workflow/`
2. Extend `AbstractFlowableTest`
3. Use helper methods: `deployProcess()`, `startProcess()`, `completeTask()`

## Test Data Builders

Use `TestDataBuilder` for creating test entities:

```java
User admin = TestDataBuilder.anAdmin().withUsername("custom-admin").buildUser();
User user = TestDataBuilder.aUser().withEmail("custom@test.com").buildUser();
```

## Custom Assertions

Use `UserAssert` for domain-specific assertions:

```java
UserAssert.assertThat(user).hasUsername("john").hasRole("ADMIN").isActive();
```

## Running Tests

```bash
# All tests
mvn verify

# Unit tests only
mvn test -Dtest="com.flowable.platform.test.unit.**"

# Integration tests only
mvn verify -Dtest="com.flowable.platform.test.integration.**"

# Single test class
mvn test -Dtest=TaskControllerIntegrationTest

# With coverage report
mvn verify jacoco:report
# Open: target/site/jacoco/index.html
```

## Timeouts

- Unit tests: 30 seconds per test (via `@Timeout` on `AbstractUnitTest`)
- Integration tests: 60 seconds per test (via `@Timeout` on `AbstractIntegrationTest`)
- Surefire fork timeout: 300 seconds
- Failsafe fork timeout: 600 seconds

## Coverage Targets

- Service layer: 80% line coverage (per constitution)
- Utilities: 60% line coverage
- Enforced by JaCoCo maven plugin during `verify` phase

## Multi-Tenant Testing

All integration tests run with a dedicated test tenant (`00000000-0000-0000-0000-000000000001`).
Tenant context is set automatically in `@BeforeEach` via `AbstractIntegrationTest`.

To test cross-tenant isolation:
```java
setTenantContext(UUID.randomUUID()); // switch to different tenant
// ... verify data isolation
clearTenantContext();
```

## Troubleshooting

- **Docker not running**: Testcontainers requires Docker Desktop
- **Port conflict**: Tests use `RANDOM_PORT` — ensure no hardcoded ports
- **Tenant context**: Extend `AbstractIntegrationTest` or call `testTenantContext.setTestTenant()`
- **Slow tests**: Enable Testcontainers reuse with `.withReuse(true)` and `testcontainers.reuse.enable=true` in `~/.testcontainers.properties`
