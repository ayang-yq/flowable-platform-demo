# flowable-platform-demo Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-03-23

## Active Technologies
- Java 21 + Spring Boot 3.5.x, Flowable 7.x, JUnit 5, Mockito 5.x, WireMock 3.x, Spring Test, Spring Boot Test (002-backend-testing)
- PostgreSQL 15+ (multi-tenant schema strategy) (002-backend-testing)
- Java 21 + Spring Boot 3.5.x, Flowable 7.x, JUnit 5, Mockito 5.x, REST Assured 5.x, Testcontainers 1.19.x, WireMock 3.x, Spring Test, Spring Boot Test (002-backend-testing)
- PostgreSQL 15+ (multi-tenant schema strategy) via Testcontainers for integration tests (002-backend-testing)
- Java 21, TypeScript 5+ + Spring Boot 3.5.x, Flowable 7.x, Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React (icons) (003-home-page-navigation)
- PostgreSQL 15+ (multi-tenant, no schema changes) (003-home-page-navigation)
- Java 21, TypeScript 5+ + Spring Boot 3.5.x, Flowable 7.2.0 (BPMN + CMMN + DMN engines), Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React, Radix UI (004-workspace-instance-dashboard)
- PostgreSQL 15+ (multi-tenant, Flowable-managed tables only — no new migrations) (004-workspace-instance-dashboard)

- Java 21, TypeScript 5+ (001-flowable-platform-core)

## Project Structure

```text
backend/
frontend/
tests/
```

## Commands

npm test; npm run lint

### Backend Test Commands
```bash
cd backend
mvn test                                                          # Unit tests
mvn verify                                                        # Unit + integration tests
mvn test -Dtest="com.flowable.platform.test.unit.**"             # Unit tests only
mvn verify -Dtest="com.flowable.platform.test.integration.**"    # Integration tests only
mvn verify jacoco:report                                          # Tests + coverage report
```

## Code Style

Java 21, TypeScript 5+: Follow standard conventions

## Testing Conventions
- Unit tests extend `AbstractUnitTest` (Mockito, 30s timeout)
- Integration tests extend `AbstractIntegrationTest` (REST Assured, Testcontainers, 60s timeout)
- Flowable tests extend `AbstractFlowableTest` (Flowable services)
- Coverage targets: 80% service layer, 60% utilities (JaCoCo enforced)
- Fail-fast: no test retries, `rerunFailingTestsCount=0`
- Test data: use `TestDataBuilder`, not raw constructors

## Recent Changes
- 004-workspace-instance-dashboard: Added Java 21, TypeScript 5+ + Spring Boot 3.5.x, Flowable 7.2.0 (BPMN + CMMN + DMN engines), Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React, Radix UI
- 003-home-page-navigation: Added Java 21, TypeScript 5+ + Spring Boot 3.5.x, Flowable 7.x, Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React (icons)
- 002-backend-testing: Added Java 21 + Spring Boot 3.5.x, Flowable 7.x, JUnit 5, Mockito 5.x, REST Assured 5.x, Testcontainers 1.19.x, WireMock 3.x, Spring Test, Spring Boot Test


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
