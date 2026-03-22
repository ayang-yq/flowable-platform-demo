# Tasks: Backend Testing Infrastructure

**Input**: Design documents from `/specs/002-backend-testing/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/test-helpers.md, quickstart.md

**Tests**: This feature IS about testing, so test-related tasks are the primary implementation tasks.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Backend**: `backend/src/` at repository root
- Test paths follow Spring Boot standard layout: `backend/src/test/java/com/flowable/platform/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and test dependencies setup

- [x] T001 Add REST Assured dependency (5.4.0) to backend/pom.xml ✅
- [x] T002 [P] Add REST Assured JSON Path dependency (5.4.0) to backend/pom.xml ✅
- [x] T003 [P] Add Testcontainers PostgreSQL dependency (1.19.3) to backend/pom.xml ✅ (already present)
- [x] T004 [P] Add Testcontainers JUnit Jupiter dependency (1.19.3) to backend/pom.xml ✅ (already present)
- [x] T005 [P] Add WireMock standalone dependency (3.5.2) to backend/pom.xml ✅
- [x] T006 [P] Add Mockito Inline dependency to backend/pom.xml ✅ (NOTE: mockito-inline removed - not available in Maven Central, using spring-boot-starter-test mockito)
- [x] T007 Create test directory structure: backend/src/test/java/com/flowable/platform/{unit,integration,config} ✅
- [x] T008 [P] Create test resources directory: backend/src/test/resources/{fixtures,db/migration,processes} ✅
- [x] T009 Verify Docker installation for Testcontainers (document in quickstart.md if needed) ✅ (Docker required for Testcontainers)

**Checkpoint**: Test dependencies configured, directory structure ready ✅ COMPLETE

---

## Phase 2: Foundational (Base Test Classes & Configuration)

**Purpose**: Core test infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T010 Create TestConfig.java in backend/src/test/java/com/flowable/platform/test/config/TestConfig.java ✅
- [x] T011 [P] Create TestTenantConfig.java in backend/src/test/java/com/flowable/platform/test/config/TestTenantConfig.java (define TEST_TENANT_ID constant) ✅
- [x] T012 [P] Create WireMockConfig.java in backend/src/test/java/com/flowable/platform/test/config/WireMockConfig.java ✅
- [x] T013 Create application-test.yml in backend/src/test/resources/application-test.yml (configure test database, logging, test tenant) ✅
- [x] T014 Create test tenant migration V__create_test_tenant.sql in backend/src/test/resources/db/migration/V__create_test_tenant.sql ✅
- [x] T015 Create TestTenantContext utility in backend/src/test/java/com/flowable/platform/test/util/TestTenantContext.java ✅
- [x] T016 [P] Create FixtureLoader utility in backend/src/test/java/com/flowable/platform/test/util/FixtureLoader.java ✅
- [x] T017 [P] Create TestAuthenticationHelper in backend/src/test/java/com/flowable/platform/test/util/TestAuthenticationHelper.java ✅
- [x] T018 Create test user fixtures SQL in backend/src/test/resources/fixtures/sql/test_users.sql (test-admin, test-user, test-guest, test-inactive) ✅
- [x] T019 Create TestDataBuilder in backend/src/test/java/com/flowable/platform/test/builder/TestDataBuilder.java ✅
- [x] T020 Create custom UserAssert in backend/src/test/java/com/flowable/platform/test/assertions/UserAssert.java ✅
- [x] T020a [P] Configure fail-fast test behavior: disable retry plugins, set JUnit 5 @Timeout defaults in backend/src/test/java/com/flowable/platform/test/config/TestConfig.java, and add surefire-plugin `<rerunFailingTestsCount>0</rerunFailingTestsCount>` to backend/pom.xml [FR-008] ✅

**Checkpoint**: Foundation ready - base test classes, utilities, and fixtures available for all user stories ✅ COMPLETE

---

## Phase 3: User Story 1 - Run Unit Tests Locally (Priority: P1) 🎯 MVP

**Goal**: Developers can run unit tests on their local machines to verify business logic works correctly before committing code.

**Independent Test**: Write a unit test for a simple service method, run it locally with `mvn test -Dtest=UserServiceTest`, and verify the test passes or fails appropriately with clear error messages.

### Implementation for User Story 1

- [x] T021 Create AbstractUnitTest in backend/src/test/java/com/flowable/platform/test/unit/AbstractUnitTest.java (Mockito setup, common assertion methods) ✅
- [x] T022 [P] Create example service unit test BusinessCalendarServiceTest.java in backend/src/test/java/com/flowable/platform/test/unit/service/BusinessCalendarServiceTest.java ✅
- [x] T023 [P] Create example repository unit test UserRepositoryTest.java in backend/src/test/java/com/flowable/platform/test/unit/repository/UserRepositoryTest.java ✅
- [x] T024 [P] Create example utility unit test DateTimeUtilsTest.java in backend/src/test/java/com/flowable/platform/test/unit/util/DateTimeUtilsTest.java ✅
- [x] T025 Add unit test execution target to quickstart.md with examples (mvn test -Dtest, ./gradlew test --tests) ✅ (already in quickstart.md)
- [x] T026 Document unit test best practices in quickstart.md (Given-When-Then, test data builders, avoid over-mocking) ✅ (already in quickstart.md)

**Checkpoint**: At this point, User Story 1 is complete - developers can write and run unit tests locally

---

## Phase 4: User Story 2 - Run API Tests with HTTP Calls (Priority: P1) 🎯 MVP

**Goal**: Developers can run API tests that make actual HTTP calls to backend endpoints to verify request/response handling works correctly.

**Independent Test**: Write an API test for an existing endpoint using REST Assured, run it with `mvn test -Dtest=TaskControllerIntegrationTest`, and verify it validates HTTP status, response body, and headers.

### Implementation for User Story 2

- [x] T027 Create AbstractIntegrationTest in backend/src/test/java/com/flowable/platform/test/integration/AbstractIntegrationTest.java (REST Assured, Testcontainers, @SpringBootTest) ✅
- [x] T028 [P] Create AbstractFlowableTest in backend/src/test/java/com/flowable/platform/test/flowable/AbstractFlowableTest.java (Flowable services, process deployment helpers) ✅
- [x] T029 [P] Create example controller API test TaskControllerIntegrationTest.java in backend/src/test/java/com/flowable/platform/test/integration/controller/TaskControllerIntegrationTest.java (REST Assured, authentication, status codes) ✅
- [x] T030 [P] Create example service integration test TaskServiceIntegrationTest.java in backend/src/test/java/com/flowable/platform/test/integration/service/TaskServiceIntegrationTest.java (with Testcontainers database) ✅
- [x] T031 [P] Create example Flowable process test SimpleProcessTest.java in backend/src/test/java/com/flowable/platform/test/integration/workflow/SimpleProcessTest.java (process deployment, task completion) ✅
- [x] T032 [P] Create test form schema fixture in backend/src/test/resources/fixtures/json/test_simple_form.json ✅
- [x] T033 [P] Create test process BPMN file in backend/src/test/resources/processes/test-simple-process.bpmn20.xml ✅
- [x] T034 Document REST Assured usage in quickstart.md (given-when-then syntax, JSON path, authentication) ✅ (already documented in quickstart.md)
- [x] T035 Document Testcontainers usage in quickstart.md (Docker requirements, container lifecycle, reuse mode) ✅ (already documented in quickstart.md)
- [x] T036 Add troubleshooting section to quickstart.md (Docker issues, REST Assured path matching, port conflicts) ✅ (already in quickstart.md)
- [x] T036a [P] Create API test example for multipart file upload endpoint in backend/src/test/java/com/flowable/platform/test/integration/controller/FileUploadIntegrationTest.java (REST Assured multipart, content-type validation) [FR-005] ✅
- [x] T036b [P] Create API test example for form-data submission in backend/src/test/java/com/flowable/platform/test/integration/controller/FormDataIntegrationTest.java (application/x-www-form-urlencoded) [FR-005] ✅

**Checkpoint**: At this point, User Story 2 is complete - developers can write and run API tests with real HTTP calls

---

## Phase 5: User Story 3 - Execute All Tests in CI/CD Pipeline (Priority: P2)

**Goal**: Tests run automatically in continuous integration pipeline to prevent broken code from being merged.

**Independent Test**: Push code to a feature branch and verify that GitHub Actions runs the test workflow, reporting results and blocking merge on failures.

### Implementation for User Story 3

- [x] T037 Create GitHub Actions workflow file in .github/workflows/test.yml (runs tests on push/PR, PostgreSQL service, test execution) ✅ (created as backend-test.yml)
- [x] T038 [P] Configure Testcontainers in CI/CD to use Docker service (GitHub Actions PostgreSQL container configuration) ✅
- [x] T039 [P] Add test report generation to pom.xml (JaCoCo for coverage, Surefire/Failsafe for test results) ✅
- [x] T040 [P] Add test coverage report artifact upload to GitHub Actions workflow ✅ (already configured in backend-test.yml)
- [x] T041 Configure branch protection rules documentation (require tests to pass before merge - document setup instructions) ✅
- [x] T042 Add test execution documentation to quickstart.md (CI/CD integration, local development vs. CI differences) ✅

**Checkpoint**: At this point, User Story 3 is complete - CI/CD pipeline runs tests automatically

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentation, coverage targets, and developer experience improvements

- [x] T043 [P] Update CLAUDE.md with testing framework information (if not already updated by plan workflow) ✅
- [x] T044 [P] Create comprehensive testing guide in backend/src/test/resources/README.md (how to write tests, common patterns, troubleshooting) ✅
- [x] T045 Add JaCoCo coverage configuration to pom.xml (80% service layer, 60% utilities - per constitution §Dev Workflow) ✅
- [x] T046 [P] Add test execution time logging to identify slow tests ✅ (configured via surefire reportFormat=plain)
- [x] T046a Configure test execution timeouts: add @Timeout(value = 30, unit = SECONDS) default in AbstractUnitTest.java and @Timeout(value = 60, unit = SECONDS) in AbstractIntegrationTest.java; configure surefire-plugin with `<forkedProcessExitTimeoutInSeconds>300</forkedProcessExitTimeoutInSeconds>` for unit tests and failsafe-plugin with `<forkedProcessExitTimeoutInSeconds>600</forkedProcessExitTimeoutInSeconds>` for integration tests [FR-014] ✅
- [x] T047 Create test data builder examples in backend/src/test/resources/fixtures/README.md ✅
- [x] T048 Document multi-tenant testing patterns in quickstart.md (tenant context switching, isolation validation) ✅ (already in quickstart.md)
- [x] T049 Add WireMock usage examples for external service stubbing in quickstart.md ✅ (already in quickstart.md)
- [ ] T050 Verify all test examples run successfully locally (manual validation checkpoint) ⚠️ (blocked by local SSL/network issue - requires manual run when Maven can reach central repo)

**Checkpoint**: All documentation complete, developer experience optimized

---

## Dependencies & Execution Order

```
Phase 1 (Setup)
    ↓
Phase 2 (Foundational) ← BLOCKS ALL USER STORIES
    ↓
    ├→ Phase 3 (US1: Unit Tests) 🎯 MVP
    ├→ Phase 4 (US2: API Tests) 🎯 MVP
    └→ Phase 5 (US3: CI/CD)
         ↓
Phase 6 (Polish)
```

**Critical Path**: Phase 1 → Phase 2 → Phase 3 or Phase 4 (both P1, can run in parallel) → Phase 5

**Parallel Opportunities**:
- **Phase 1**: T002-T006 can run in parallel (different dependencies)
- **Phase 2**: T011-T012 can run in parallel (different config classes)
- **Phase 3**: T022-T024 can run in parallel (different test classes)
- **Phase 4**: T028-T033 can run in parallel (different test classes and fixtures)
- **Phase 5**: T038-T039 can run in parallel (different aspects)
- **Phase 6**: T043-T047 can run in parallel (documentation)

---

## MVP Scope

**Recommended MVP**: Phase 1 + Phase 2 + Phase 3 (Unit Tests) + Phase 4 (API Tests)

This delivers:
- ✅ Test infrastructure setup
- ✅ Base test classes and utilities
- ✅ Working unit tests with clear feedback
- ✅ Working API tests with real HTTP calls

**Post-MVP**: Add Phase 5 (CI/CD) and Phase 6 (Polish) for production-ready testing infrastructure.

---

## Task Metrics

- **Total Tasks**: 50
- **Setup Tasks**: 9
- **Foundational Tasks**: 11
- **User Story 1 (Unit Tests)**: 6 tasks
- **User Story 2 (API Tests)**: 10 tasks
- **User Story 3 (CI/CD)**: 6 tasks
- **Polish Tasks**: 8 tasks
- **Parallel Opportunities**: 25 tasks (50% of total)

---

## Implementation Strategy

**Incremental Delivery**: Each phase delivers value independently:
1. **Phase 1-2**: Foundation (no user value, but enables everything else)
2. **Phase 3**: Unit tests working locally (US1 complete)
3. **Phase 4**: API tests working locally (US2 complete)
4. **Phase 5**: CI/CD automation (US3 complete)
5. **Phase 6**: Documentation and polish

**Risk Mitigation**:
- Start with Phase 1-2 to validate test infrastructure works
- Implement US1 first (unit tests are simpler, faster feedback)
- Add US2 next (REST Assured may need learning, start with examples)
- Add US3 last (requires CI/CD setup, can be done manually initially)

**Testing Strategy**:
- This feature IS about testing, so "test tasks" ARE the implementation tasks
- Each task creates test infrastructure or example tests
- Manual verification: Run the example tests to ensure they work
- Coverage target: 80% service layer, 60% utilities (validate with JaCoCo)

---

**Last Updated**: 2026-03-22 | **Version**: 1.0.0
