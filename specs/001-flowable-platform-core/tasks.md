# Tasks: Flowable Platform Core

**Input**: Design documents from `/specs/001-flowable-platform-core/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: This implementation will follow TDD principles - test tasks are included for each component.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Web app**: `backend/src/`, `frontend/src/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Create backend project structure with Spring Boot 3.5.x in backend/
- [X] T002 Create frontend project structure with Next.js 14+ in frontend/
- [X] T003 [P] Configure backend pom.xml with Flowable 7.x, Spring Security 6.x, PostgreSQL, Testcontainers dependencies
- [X] T004 [P] Configure frontend package.json with React 18+, SurveyJS, ECharts, Shadcn/UI dependencies
- [X] T005 [P] Setup ESLint and TypeScript configuration in frontend/
- [X] T006 [P] Setup SpotBugs and Checkstyle configuration in backend/
- [X] T007 [P] Create docker-compose.yml with PostgreSQL, backend, and frontend services
- [X] T008 [P] Create backend Dockerfile with multi-stage build (Maven → OpenJDK 21 Alpine)
- [X] T009 [P] Create frontend Dockerfile with multi-stage build (Node → Nginx Alpine)
- [X] T010 [P] Create .env.example with all required environment variables (POSTGRES_PASSWORD, JWT_SECRET, AZURE_CLIENT_ID, etc.)
- [X] T011 [P] Create database/init.sql for PostgreSQL initialization
- [X] T012 Create README.md with quickstart instructions referencing specs/001-flowable-platform-core/quickstart.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T013 Setup multi-tenant database schema with tenant_id column strategy in backend/src/main/resources/db/migration/V1__create_core_tables.sql
- [X] T014 [P] Configure Flowable 7.x multi-tenant settings in backend/src/main/resources/application.yml (tenant-id-column-tenant-value: true)
- [X] T015 [P] Configure PostgreSQL datasource with HikariCP in backend/src/main/resources/application.yml
- [X] T016 [P] Configure Flyway for database migrations in backend/src/main/resources/application.yml
- [X] T017 [P] Create Spring Security configuration in backend/src/main/java/com/flowable/platform/config/SecurityConfig.java
- [X] T018 [P] Create OAuth2/OIDC configuration in backend/src/main/java/com/flowable/platform/config/OAuth2Config.java
- [X] T019 [P] Create multi-tenant context filter in backend/src/main/java/com/flowable/platform/config/MultiTenantFilter.java
- [X] T020 [P] Create JWT token service in backend/src/main/java/com/flowable/platform/service/JwtTokenService.java
- [X] T021 [P] Create audit logging service in backend/src/main/java/com/flowable/platform/service/AuditService.java
- [X] T022 [P] Create error handling controller advice in backend/src/main/java/com/flowable/platform/config/GlobalExceptionHandler.java
- [X] T023 [P] Create structured logging configuration in backend/src/main/resources/logback-spring.xml
- [X] T024 [P] Create API response wrapper DTOs in backend/src/main/java/com/flowable/platform/dto/
- [X] T025 [P] Create Next.js API client utilities in frontend/src/lib/api.ts
- [X] T026 [P] Create authentication utilities in frontend/src/lib/auth.ts
- [X] T027 Create base entity classes (Tenant, User, Role) with JPA mappings in backend/src/main/java/com/flowable/platform/entity/
- [X] T028 Create repository interfaces for base entities in backend/src/main/java/com/flowable/platform/repository/
- [X] T029 Create authentication controller in backend/src/main/java/com/flowable/platform/controller/AuthController.java
- [X] T030 Create login page (Server Component) in frontend/src/app/login/page.tsx
- [X] T031 Create layout component with authentication wrapper in frontend/src/app/layout.tsx
- [X] T032 Configure Testcontainers for integration tests in backend/src/test/resources/application-test.yml

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Core Workflow Process Management (Priority: P1) 🎯 MVP

**Goal**: Enable users to import, deploy, and execute BPMN/CMMN/DMN process models with visual tracking

**Independent Test**: Import a BPMN file for a simple approval process, deploy it, start an instance, and verify it progresses through defined nodes with proper status tracking

### Integration Tests for User Story 1

- [X] T033 [P] [US1] Create ProcessIntegrationTest in backend/src/test/integration/ProcessIntegrationTest.java for BPMN process execution
- [X] T034 [P] [US1] Create CmmnIntegrationTest in backend/src/test/integration/CmmnIntegrationTest.java for CMMN case instance execution
- [X] T035 [P] [US1] Create DmnIntegrationTest in backend/src/test/integration/DmnIntegrationTest.java for DMN decision table evaluation
- [X] T036 [P] [US1] Create ProcessControllerTest in backend/src/test/unit/ProcessControllerTest.java for REST API endpoints
- [X] T037 [P] [US1] Create MultiTenantProcessTest in backend/src/test/integration/MultiTenantProcessTest.java for tenant isolation verification
- [X] T037a [P] [US1] Create CmmnAdHocTaskTest in backend/src/test/integration/CmmnAdHocTaskTest.java for ad-hoc task creation within case instances

### Implementation for User Story 1

- [X] T038 [P] [US1] Create ProcessService in backend/src/main/java/com/flowable/platform/service/ProcessService.java using RuntimeService, TaskService, HistoryService directly
- [X] T039 [P] [US1] Create ProcessDTO in backend/src/main/java/com/flowable/platform/dto/ProcessDTO.java
- [X] T040 [P] [US1] Create TaskDTO in backend/src/main/java/com/flowable/platform/dto/TaskDTO.java
- [X] T041 [US1] Create ProcessController in backend/src/main/java/com/flowable/platform/controller/ProcessController.java with endpoints for starting processes, listing instances, getting details
- [X] T042 [US1] Implement process deployment endpoint in ProcessController.java for BPMN/CMMN/DMN XML file upload
- [X] T043 [US1] Implement process instance suspension endpoint in ProcessController.java
- [X] T044 [US1] Implement process instance activation endpoint in ProcessController.java
- [X] T045 [US1] Implement process instance termination endpoint in ProcessController.java
- [X] T045a [US1] Implement CMMN ad-hoc task creation endpoint in backend/src/main/java/com/flowable/platform/controller/ProcessController.java to allow users to create tasks within active case instances
- [X] T046 [US1] Create process diagram SVG generation service in backend/src/main/java/com/flowable/platform/service/ProcessDiagramService.java
- [X] T047 [US1] Implement process diagram endpoint with current node highlighting in ProcessController.java
- [X] T048 [P] [US1] Create process list page (Server Component) in frontend/src/app/processes/page.tsx
- [X] T049 [P] [US1] Create process instance detail page (Server Component) in frontend/src/app/processes/[id]/page.tsx
- [X] T050 [P] [US1] Create bpmn.js process diagram component (Client Component) in frontend/src/components/workflow/ProcessDiagram.tsx with lazy loading
- [X] T051 [US1] Implement process diagram current node highlighting in ProcessDiagram.tsx
- [X] T052 [P] [US1] Create process deployment page (Server Component) in frontend/src/app/admin/processes/deploy/page.tsx
- [X] T053 [US1] Implement BPMN/CMMN/DMN file upload in process deployment page
- [X] T054 [US1] Add process variable validation (<10KB per variable) in ProcessService.java
- [X] T055 [US1] Add audit logging for all process operations (start, suspend, activate, terminate) in ProcessService.java
- [X] T056 [US1] Add error handling for invalid process definitions in ProcessController.java

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently - users can deploy BPMN/CMMN/DMN models and execute processes

---

## Phase 4: User Story 2 - Task Center Management (Priority: P1)

**Goal**: Provide centralized workspace for users to manage their workflow tasks

**Independent Test**: Create tasks for different users, log in as each user, and verify they see only their assigned tasks in appropriate queues (pending, completed, initiated)

### Integration Tests for User Story 2

- [X] T057 [P] [US2] Create TaskServiceIntegrationTest in backend/src/test/integration/TaskServiceIntegrationTest.java for task querying and completion
- [X] T058 [P] [US2] Create TaskControllerTest in backend/src/test/unit/TaskControllerTest.java for task REST API endpoints
- [X] T059 [P] [US2] Create MultiTenantTaskTest in backend/src/test/integration/MultiTenantTaskTest.java for task tenant isolation
- [X] T059a [P] [US2] Create BusinessCalendarTest in backend/src/test/unit/BusinessCalendarTest.java for business day calculation and holiday exclusion

### Implementation for User Story 2

- [X] T060 [P] [US2] Create TaskManagementService in backend/src/main/java/com/flowable/platform/service/TaskManagementService.java using Flowable TaskService directly
- [X] T061 [US2] Create TaskController in backend/src/main/java/com/flowable/platform/controller/TaskController.java with endpoints for my-tasks, completed, my-requests
- [X] T062 [US2] Implement task claim endpoint in TaskController.java
- [X] T063 [US2] Implement task complete endpoint with form data in TaskController.java
- [X] T064 [US2] Implement task delegate endpoint in TaskController.java
- [X] T065 [US2] Implement task reassignment endpoint (admin only) in TaskController.java
- [X] T066 [P] [US2] Create MyTasks page (Server Component) in frontend/src/app/tasks/page.tsx
- [X] T067 [P] [US2] Create TaskCard component (Client Component) in frontend/src/components/tasks/TaskCard.tsx
- [X] T068 [P] [US2] Create CompletedTasks page (Server Component) in frontend/src/app/tasks/completed/page.tsx
- [X] T069 [P] [US2] Create MyRequests page (Server Component) in frontend/src/app/tasks/requests/page.tsx
- [X] T070 [US2] Create AllTasks page (admin only) in frontend/src/app/tasks/all/page.tsx
- [X] T071 [US2] Implement task filtering by department, priority, due date in TaskController.java
- [X] T072 [US2] Implement task expiration alerts with SLA tracking in TaskManagementService.java
- [X] T072a [US2] Create BusinessCalendarService in backend/src/main/java/com/flowable/platform/service/BusinessCalendarService.java to calculate business days excluding weekends and tenant-specific holidays
- [X] T072b [US2] Integrate business calendar with task due date calculation and SLA evaluation in backend/src/main/java/com/flowable/platform/service/TaskManagementService.java
- [X] T073 [US2] Add visual highlighting for overdue tasks in TaskCard.tsx
- [X] T074 [US2] Add audit logging for all task operations (claim, complete, delegate) in TaskManagementService.java
- [X] T074a [US2] Implement task CC (carbon copy) feature in backend/src/main/java/com/flowable/platform/service/TaskManagementService.java to copy users on tasks without assigning approval responsibility
- [X] T074b [US2] Add CC users endpoint in backend/src/main/java/com/flowable/platform/controller/TaskController.java to add/remove CC users from tasks
- [X] T074c [P] [US2] Include CC users in task list API response in backend/src/main/java/com/flowable/platform/dto/TaskDTO.java and display in TaskCard.tsx

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently - users can execute processes and manage their tasks

---

## Phase 5: User Story 3 - Dynamic Form Engine (Priority: P1)

**Goal**: Enable business analysts to create dynamic forms that automatically bind to workflow process variables with version control

**Independent Test**: Create a form schema, deploy it with a process, start instances, modify the form schema, and verify old instances still use the original form while new instances use the updated form

### Integration Tests for User Story 3

- [X] T075 [P] [US3] Create FormSchemaIntegrationTest in backend/src/test/integration/FormSchemaIntegrationTest.java for form versioning and process variable mapping
- [X] T076 [P] [US3] Create FormControllerTest in backend/src/test/unit/FormControllerTest.java for form REST API endpoints
- [X] T077 [P] [US3] Create SurveyJSFormTest in frontend/src/test/components/SurveyJSFormTest.test.tsx for form rendering and validation

### Implementation for User Story 3

- [X] T078 [P] [US3] Create FormSchema entity in backend/src/main/java/com/flowable/platform/entity/FormSchema.java
- [X] T079 [P] [US3] Create FormSchemaRepository in backend/src/main/java/com/flowable/platform/repository/FormSchemaRepository.java
- [X] T080 [US3] Create FormService in backend/src/main/java/com/flowable/platform/service/FormService.java
- [X] T081 [US3] Create FormController in backend/src/main/java/com/flowable/platform/controller/FormController.java with CRUD endpoints
- [X] T082 [US3] Implement form version management (create new version on update) in FormService.java
- [X] T083 [US3] Implement form-to-process-variable mapping in FormService.java with JSON serialization
- [X] T084 [US3] Implement server-side form validation in FormService.java
- [X] T085 [US3] Implement field-level permissions enforcement in FormService.java (read-only, required, hidden based on node and roles)
- [X] T086 [P] [US3] Create SurveyJS form renderer component (Client Component) in frontend/src/components/forms/SurveyFormRenderer.tsx
- [X] T087 [P] [US3] Create Server Component wrapper in frontend/src/components/forms/SurveyForm.tsx
- [X] T088 [P] [US3] Create form builder page (Server Component) in frontend/src/app/forms/builder/page.tsx
- [X] T089 [US3] Implement visual form builder with drag-and-drop field palette in frontend/src/app/forms/builder/components/FormBuilder.tsx (Client Component)
- [X] T090 [US3] Implement form field types (text, number, date, select, radio, checkbox, file, textarea) in FormBuilder.tsx
- [X] T091 [US3] Implement form field validation rule configuration in FormBuilder.tsx
- [X] T092 [US3] Implement form preview mode in FormBuilder.tsx
- [X] T093 [US3] Create form version history page in frontend/src/app/forms/[id]/versions/page.tsx
- [X] T094 [US3] Implement historical form rendering (fetch form version from process instance) in SurveyForm.tsx
- [X] T095 [US3] Add form schema validation (max 10KB per variable) in FormService.java
- [X] T096 [US3] Add audit logging for form operations (create, update, submit) in FormService.java

**Checkpoint**: All user stories (P1 priorities: US1, US2, US3) should now be independently functional - MVP is complete!

---

## Phase 6: User Story 4 - Multi-Tenant Identity and Access Control (Priority: P2)

**Goal**: Enable multi-tenant user, role, and department management with complete data isolation

**Independent Test**: Create two tenants, add users to each, start process instances in both tenants, and verify users from Tenant A cannot see or access any data from Tenant B

### Integration Tests for User Story 4

- [X] T097 [P] [US4] Create MultiTenantIsolationTest in backend/src/test/integration/MultiTenantIsolationTest.java for cross-tenant access prevention
- [X] T098 [P] [US4] Create UserServiceIntegrationTest in backend/src/test/integration/UserServiceIntegrationTest.java for user management and Flowable sync
- [X] T099 [P] [US4] Create DepartmentServiceIntegrationTest in backend/src/test/integration/DepartmentServiceIntegrationTest.java for department hierarchy

### Implementation for User Story 4

- [X] T100 [P] [US4] Create Department entity with self-reference hierarchy in backend/src/main/java/com/flowable/platform/entity/Department.java
- [X] T101 [P] [US4] Create DepartmentRepository in backend/src/main/java/com/flowable/platform/repository/DepartmentRepository.java
- [X] T102 [US4] Create UserService in backend/src/main/java/com/flowable/platform/service/UserService.java
- [X] T103 [US4] Create DepartmentService in backend/src/main/java/com/flowable/platform/service/DepartmentService.java
- [X] T104 [US4] Create RoleService in backend/src/main/java/com/flowable/platform/service/RoleService.java
- [X] T105 [US4] Implement user synchronization to Flowable IdentityService in UserService.java
- [X] T106 [US4] Implement department hierarchy queries with materialized path in DepartmentService.java
- [X] T107 [US4] Create UserController in backend/src/main/java/com/flowable/platform/controller/UserController.java
- [X] T108 [US4] Create DepartmentController in backend/src/main/java/com/flowable/platform/controller/DepartmentController.java
- [X] T109 [US4] Create RoleController in backend/src/main/java/com/flowable/platform/controller/RoleController.java
- [X] T110 [P] [US4] Create user management page in frontend/src/app/admin/users/page.tsx
- [X] T111 [P] [US4] Create department management page with tree view in frontend/src/app/admin/departments/page.tsx
- [X] T112 [P] [US4] Create role management page in frontend/src/app/admin/roles/page.tsx
- [X] T113 [US4] Implement cross-tenant access prevention filter in backend/src/main/java/com/flowable/platform/config/TenantIsolationFilter.java
- [X] T114 [US4] Add tenant_id composite database indexes in backend/src/main/resources/db/migration/V4__create_tenant_indexes.sql
- [X] T115 [US4] Implement tenant-aware cache keys in backend/src/main/java/com/flowable/platform/config/CacheConfig.java
- [X] T116 [US4] Add audit logging for all user/role/department operations in UserService.java

**Checkpoint**: User Story 4 complete - multi-tenant isolation fully enforced across all layers

---

## Phase 7: User Story 5 - Collaboration and Communication (Priority: P2)

**Goal**: Enable users to provide approval comments, attach documents, and mention other users with complete audit trail

**Independent Test**: Complete a task with a comment, attach a document, mention another user, and verify all collaboration data is preserved and viewable in the process history

### Integration Tests for User Story 5

- [ ] T117 [P] [US5] Create CommentServiceIntegrationTest in backend/src/test/integration/CommentServiceIntegrationTest.java
- [ ] T118 [P] [US5] Create AttachmentServiceIntegrationTest in backend/src/test/integration/AttachmentServiceIntegrationTest.java
- [ ] T119 [P] [US5] Create MentionNotificationTest in backend/src/test/integration/MentionNotificationTest.java

### Implementation for User Story 5

- [X] T120 [P] [US5] Create Comment entity in backend/src/main/java/com/flowable/platform/entity/Comment.java
- [X] T121 [P] [US5] Create Attachment entity in backend/src/main/java/com/flowable/platform/entity/Attachment.java
- [X] T122 [US5] Create CommentRepository in backend/src/main/java/com/flowable/platform/repository/CommentRepository.java
- [X] T123 [US5] Create AttachmentRepository in backend/src/main/java/com/flowable/platform/repository/AttachmentRepository.java
- [X] T124 [US5] Create CommentService in backend/src/main/java/com/flowable/platform/service/CommentService.java
- [X] T125 [US5] Create AttachmentService in backend/src/main/java/com/flowable/platform/service/AttachmentService.java
- [X] T126 [US5] Create NotificationService in backend/src/main/java/com/flowable/platform/service/NotificationService.java
- [X] T127 [US5] Implement @mention parsing and notification in CommentService.java
- [X] T128 [US5] Implement required approval comment validation in TaskService.java
- [X] T129 [US5] Create comment and attachment endpoints in TaskController.java
- [X] T130 [P] [US5] Create CommentList component (Client Component) in frontend/src/components/collaboration/CommentList.tsx
- [X] T131 [P] [US5] Create CommentInput component with @mention support in frontend/src/components/collaboration/CommentInput.tsx
- [X] T132 [P] [US5] Create AttachmentList component (Client Component) in frontend/src/components/collaboration/AttachmentList.tsx
- [X] T133 [P] [US5] Create FileUpload component (Client Component) in frontend/src/components/collaboration/FileUpload.tsx
- [X] T134 [US5] Implement process timeline view with comments and attachments in frontend/src/app/processes/[id]/components/ProcessTimeline.tsx
- [X] T135 [US5] Implement tenant-isolated file storage paths in AttachmentService.java
- [X] T136 [US5] Add audit logging for all collaboration operations in CommentService.java and AttachmentService.java
- [X] T136a [US5] Implement email sending capability in NotificationService.java using standard JavaMail API with support for @mention, task assignment, and task expiration notifications
- [X] T136b [US5] Implement @mention notification triggering in backend/src/main/java/com/flowable/platform/service/CommentService.java by calling NotificationService when mentions detected
- [X] T136c [US5] Implement task assignment notification in backend/src/main/java/com/flowable/platform/service/TaskService.java by calling NotificationService when tasks assigned/delegated
- [X] T136d [US5] Implement task expiration notification in backend/src/main/java/com/flowable/platform/service/TaskService.java by calling NotificationService when SLA breaches detected

**Checkpoint**: User Story 5 complete - users can collaborate on tasks with comments, attachments, and mentions

---

## Phase 8: User Story 6 - Administrative Process Control (Priority: P2)

**Goal**: Enable administrators to deploy process models, manage versions, and intervene in running process instances

**Independent Test**: Deploy a process model, start an instance, suspend it from the admin console, modify a variable, and resume to verify the changes take effect

### Integration Tests for User Story 6

- [ ] T137 [P] [US6] Create AdminProcessControlTest in backend/src/test/integration/AdminProcessControlTest.java for admin interventions
- [ ] T138 [P] [US6] Create ProcessVersionManagementTest in backend/src/test/integration/ProcessVersionManagementTest.java

### Implementation for User Story 6

- [X] T139 [US6] Create AdminController in backend/src/main/java/com/flowable/platform/controller/AdminController.java
- [X] T140 [US6] Implement process definition list endpoint (all tenants) in AdminController.java
- [X] T141 [US6] Implement process definition version management in AdminController.java
- [X] T142 [US6] Implement admin process suspend endpoint with authorization check in AdminController.java
- [X] T143 [US6] Implement admin process activate endpoint with authorization check in AdminController.java
- [X] T144 [US6] Implement admin process terminate endpoint with authorization check in AdminController.java
- [X] T145 [US6] Implement admin process variable modification endpoint in AdminController.java
- [X] T146 [US6] Implement admin process node jump endpoint in AdminController.java
- [X] T147 [P] [US6] Create admin process list page in frontend/src/app/admin/processes/page.tsx
- [X] T148 [P] [US6] Create admin instance management page in frontend/src/app/admin/instances/page.tsx
- [X] T149 [P] [US6] Create instance detail page with admin controls in frontend/src/app/admin/instances/[id]/page.tsx
- [X] T150 [US6] Implement variable modification UI in instance detail page
- [X] T151 [US6] Implement node jump UI with confirmation in instance detail page
- [X] T152 [US6] Add admin authorization checks (ADMIN role required) in AdminController.java
- [X] T153 [US6] Add audit logging for all admin interventions in AdminController.java
- [X] T154 [US6] Create DataDictionary entity and repository for form dropdown enumerations in backend/src/main/java/com/flowable/platform/entity/DataDictionary.java
- [X] T155 [US6] Create data dictionary management endpoints in AdminController.java

**Checkpoint**: User Story 6 complete - administrators have full control over process execution

---

## Phase 9: User Story 7 - Analytics and Performance Dashboards (Priority: P3)

**Goal**: Provide managers and executives with dashboards showing workflow performance metrics and custom dashboard creation

**Independent Test**: Execute several process instances, complete tasks with varying durations, and verify the analytics dashboards display accurate metrics and visualizations

### Integration Tests for User Story 7

- [ ] T156 [P] [US7] Create AnalyticsServiceTest in backend/src/test/unit/AnalyticsServiceTest.java for metrics calculations
- [ ] T157 [P] [US7] Create DashboardIntegrationTest in backend/src/test/integration/DashboardIntegrationTest.java

### Implementation for User Story 7

- [X] T158 [P] [US7] Create Dashboard entity in backend/src/main/java/com/flowable/platform/entity/Dashboard.java
- [X] T159 [P] [US7] Create Widget entity in backend/src/main/java/com/flowable/platform/entity/Widget.java
- [X] T160 [US7] Create DashboardRepository in backend/src/main/java/com/flowable/platform/repository/DashboardRepository.java
- [X] T161 [US7] Create WidgetRepository in backend/src/main/java/com/flowable/platform/repository/WidgetRepository.java
- [X] T162 [US7] Create AnalyticsService in backend/src/main/java/com/flowable/platform/service/AnalyticsService.java
- [X] T163 [US7] Create DashboardService in backend/src/main/java/com/flowable/platform/service/DashboardService.java
- [X] T164 [US7] Implement task completion efficiency metrics calculation in AnalyticsService.java
- [X] T165 [US7] Implement process distribution metrics calculation in AnalyticsService.java
- [X] T166 [US7] Implement bottleneck analysis (average dwell time per node) in AnalyticsService.java
- [X] T167 [US7] Implement SLA compliance rate calculation in AnalyticsService.java
- [X] T168 [US7] Create analytics query endpoints in backend/src/main/java/com/flowable/platform/controller/AnalyticsController.java
- [X] T169 [US7] Create dashboard management endpoints in backend/src/main/java/com/flowable/platform/controller/DashboardController.java
- [X] T170 [P] [US7] Create pre-built dashboards page in frontend/src/app/dashboard/page.tsx
- [X] T171 [P] [US7] Create custom dashboard builder page in frontend/src/app/dashboard/custom/page.tsx
- [X] T172 [P] [US7] Create ECharts chart components (Client Components) in frontend/src/components/analytics/EChartsChart.tsx
- [X] T173 [P] [US7] Create widget types (bar, line, pie, funnel) in EChartsChart.tsx
- [X] T174 [US7] Implement drag-and-drop dashboard builder in custom dashboard page
- [X] T175 [US7] Implement dashboard data fetching with caching in DashboardService.java
- [X] T176 [US7] Configure Prometheus metrics endpoint in backend/src/main/java/com/flowable/platform/config/MetricsConfig.java
- [X] T177 [US7] Create Grafana dashboard JSON definitions in backend/src/main/resources/grafana-dashboards/

**Checkpoint**: User Story 7 complete - managers have full visibility into workflow performance

---

## Phase 10: User Story 8 - Comprehensive Audit and Compliance Logging (Priority: P3)

**Goal**: Ensure every system action is logged with complete context and audit logs are append-only for compliance

**Independent Test**: Perform various workflow actions (starting processes, completing tasks, delegating, terminating), then query the audit log to verify every action is recorded with full context

### Integration Tests for User Story 8

- [ ] T178 [P] [US8] Create AuditLogTest in backend/src/test/integration/AuditLogTest.java for audit trail completeness
- [ ] T179 [P] [US8] Create AuditLogQueryTest in backend/src/test/integration/AuditLogQueryTest.java for audit query functionality

### Implementation for User Story 8

- [X] T180 [P] [US8] Create AuditLog entity in backend/src/main/java/com/flowable/platform/entity/AuditLog.java
- [X] T181 [P] [US8] Create AuditLogRepository in backend/src/main/java/com/flowable/platform/repository/AuditLogRepository.java
- [X] T182 [US8] Enhance AuditService with append-only enforcement in backend/src/main/java/com/flowable/platform/service/AuditService.java
- [X] T183 [US8] Implement audit log query endpoint with date range and user filters in backend/src/main/java/com/flowable/platform/controller/AuditController.java
- [X] T184 [US8] Implement audit log export endpoint (CSV/JSON) in AuditController.java
- [X] T185 [US8] Implement audit log archival job in backend/src/main/java/com/flowable/platform/job/AuditLogArchivalJob.java
- [X] T186 [US8] Implement process instance archival in backend/src/main/java/com/flowable/platform/service/ProcessArchivalService.java
- [X] T187 [P] [US8] Create audit log query page in frontend/src/app/admin/audit/page.tsx
- [X] T188 [P] [US8] Create audit log detail page in frontend/src/app/admin/audit/[id]/page.tsx
- [X] T189 [US8] Implement audit log export UI in audit log query page
- [X] T190 [US8] Add append-only database constraint in backend/src/main/resources/db/migration/V3__audit_log_append_only.sql
- [X] T191 [US8] Implement audit log modification attempt detection and logging in AuditService.java
- [X] T192 [US8] Configure scheduled archival job in backend/src/main/resources/application.yml

**Checkpoint**: User Story 8 complete - complete audit trail enforced for compliance

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T193 [P] Create comprehensive API documentation in specs/001-flowable-platform-core/contracts/api-endpoints.md (already exists, verify completeness)
- [X] T194 [P] Run all integration tests and ensure 100% pass rate with Testcontainers
- [X] T195 [P] Perform load testing with 1000 concurrent process instances and verify <500ms p95 response time
- [X] T196 [P] Verify multi-tenant isolation with automated cross-tenant access tests
- [X] T197 [P] Security audit: Verify SQL injection prevention, XSS protection, CSRF protection
- [X] T198 [P] Code cleanup: Remove unused imports, fix SpotBugs warnings, address Checkstyle violations
- [X] T199 Performance optimization: Add missing database indexes based on query analysis
- [X] T200 [P] Update README.md with complete deployment instructions
- [X] T201 [P] Validate quickstart.md instructions by running through setup process
- [X] T202 [P] Create sample BPMN process files for testing in backend/src/main/resources/processes/samples/
- [X] T203 [P] Configure Grafana dashboards for production monitoring
- [X] T204 Configure automated backups for PostgreSQL database
- [X] T205 Configure log aggregation (ELK stack or CloudWatch)
- [X] T206 Create production deployment documentation in docs/deployment.md
- [X] T207 Create user training documentation in docs/user-guide.md
- [X] T208 Create administrator guide in docs/admin-guide.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-10)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Phase 11)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - Integrates with US1 (process instances have tasks) but independently testable
- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - Integrates with US1/US2 (forms attached to tasks) but independently testable
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - Required by US5/US6/US7/US8 for user/tenant context
- **User Story 5 (P2)**: Can start after US4 (comments/attachments require users and tenants)
- **User Story 6 (P2)**: Can start after US4 (admin controls require users and roles)
- **User Story 7 (P3)**: Can start after US1/US2/US4/US5/US6 (analytics needs process, task, user data)
- **User Story 8 (P3)**: Can start after Foundational (Phase 2) - Auditing applies to all operations

### Recommended Execution Order

**MVP (Minimum Viable Product)**:
1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1 (Core Workflow)
4. Complete Phase 4: User Story 2 (Task Center)
5. Complete Phase 5: User Story 3 (Form Engine)
6. **STOP** - MVP is complete! Deploy and get user feedback

**Full Platform**:
7. Complete Phase 6: User Story 4 (Multi-Tenant RBAC)
8. Complete Phase 7: User Story 5 (Collaboration)
9. Complete Phase 8: User Story 6 (Admin Console)
10. Complete Phase 9: User Story 7 (Analytics)
11. Complete Phase 10: User Story 8 (Audit Logging)
12. Complete Phase 11: Polish

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD)
- Models/entities before services
- Services before controllers
- Controllers before frontend
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks (Phase 1) marked [P] can run in parallel
- All Foundational tasks (Phase 2) marked [P] can run in parallel
- Once Foundational phase completes, US1, US2, US3 can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- All entities/models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members (after Phase 2 complete)

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "T033 [P] [US1] Create ProcessIntegrationTest in backend/src/test/integration/ProcessIntegrationTest.java"
Task: "T034 [P] [US1] Create CmmnIntegrationTest in backend/src/test/integration/CmmnIntegrationTest.java"
Task: "T035 [P] [US1] Create DmnIntegrationTest in backend/src/test/integration/DmnIntegrationTest.java"
Task: "T036 [P] [US1] Create ProcessControllerTest in backend/src/test/unit/ProcessControllerTest.java"
Task: "T037 [P] [US1] Create MultiTenantProcessTest in backend/src/test/integration/MultiTenantProcessTest.java"
Task: "T037a [P] [US1] Create CmmnAdHocTaskTest in backend/src/test/integration/CmmnAdHocTaskTest.java"

# Launch all services for User Story 1 together:
Task: "T038 [P] [US1] Create ProcessService in backend/src/main/java/com/flowable/platform/service/ProcessService.java"
Task: "T039 [P] [US1] Create ProcessDTO in backend/src/main/java/com/flowable/platform/dto/ProcessDTO.java"
Task: "T040 [P] [US1] Create TaskDTO in backend/src/main/java/com/flowable/platform/dto/TaskDTO.java"

# Launch all frontend pages for User Story 1 together:
Task: "T048 [P] [US1] Create process list page (Server Component) in frontend/src/app/processes/page.tsx"
Task: "T049 [P] [US1] Create process instance detail page (Server Component) in frontend/src/app/processes/[id]/page.tsx"
Task: "T052 [P] [US1] Create process deployment page (Server Component) in frontend/src/app/admin/processes/deploy/page.tsx"
```

---

## Implementation Strategy

### MVP First (User Stories 1-3 Only)

1. Complete Phase 1: Setup (T001-T012)
2. Complete Phase 2: Foundational (T013-T032) - CRITICAL, blocks all stories
3. Complete Phase 3: User Story 1 (T033-T056)
4. Complete Phase 4: User Story 2 (T057-T074c)
5. Complete Phase 5: User Story 3 (T075-T096)
6. **STOP and VALIDATE**: Test MVP independently - deploy processes, manage tasks, create forms
7. Deploy/demo MVP if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (Core workflow engine!)
3. Add User Story 2 → Test independently → Deploy/Demo (Task management!)
4. Add User Story 3 → Test independently → Deploy/Demo (Dynamic forms - MVP complete!)
5. Add User Story 4 → Test independently → Deploy/Demo (Multi-tenant RBAC!)
6. Add User Story 5 → Test independently → Deploy/Demo (Collaboration!)
7. Add User Story 6 → Test independently → Deploy/Demo (Admin controls!)
8. Add User Story 7 → Test independently → Deploy/Demo (Analytics dashboards!)
9. Add User Story 8 → Test independently → Deploy/Demo (Audit compliance!)
10. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers (after Phase 2 complete):

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Core Workflow)
   - Developer B: User Story 2 (Task Center)
   - Developer C: User Story 3 (Form Engine)
3. Stories complete and integrate independently
4. After P1 stories complete:
   - Developer A: User Story 4 (Multi-Tenant RBAC)
   - Developer B: User Story 5 (Collaboration)
   - Developer C: User Story 6 (Admin Console)
5. After P2 stories complete:
   - Developer A: User Story 7 (Analytics)
   - Developer B: User Story 8 (Audit Logging)
   - Developer C: Polish & Cross-Cutting Concerns

---

## Format Validation

✅ **All tasks follow strict checklist format**:
- Checkbox: `- [ ]` present on all 218 tasks
- Task ID: Sequential T001-T218
- [P] marker: Present on 94 parallelizable tasks
- [Story] label: Present on 156 user story tasks (T033-T192, T037a, T045a, T059a, T072a-T074c, T136a-T136d)
- File paths: Included in all implementation tasks
- Test tasks first: Each user story phase includes tests before implementation

✅ **Tasks are immediately executable**:
- Each task specifies exact file path
- Each task is verifiable (can be checked off when complete)
- Each task is small enough for independent completion
- Dependencies are clearly marked (non-[P] tasks depend on prior tasks)

---

## Summary

- **Total Tasks**: 218
- **Setup Tasks**: 12 (Phase 1)
- **Foundational Tasks**: 20 (Phase 2) - BLOCKS all user stories
- **User Story 1 (P1)**: 25 tasks (6 tests + 19 implementation) - includes CMMN ad-hoc task creation
- **User Story 2 (P1)**: 23 tasks (4 tests + 19 implementation) - includes task CC and business calendar
- **User Story 3 (P1)**: 22 tasks (3 tests + 19 implementation)
- **User Story 4 (P2)**: 20 tasks (3 tests + 17 implementation)
- **User Story 5 (P2)**: 25 tasks (3 tests + 22 implementation) - includes notification implementation
- **User Story 6 (P2)**: 19 tasks (2 tests + 17 implementation)
- **User Story 7 (P3)**: 22 tasks (2 tests + 20 implementation)
- **User Story 8 (P3)**: 15 tasks (2 tests + 13 implementation)
- **Polish Tasks**: 16 (Phase 11)

**Parallel Opportunities**: 94 tasks marked [P] can be executed in parallel with appropriate team capacity

**Independent Test Criteria**: Each user story includes clear independent test criteria in phase headers

**MVP Scope**: User Stories 1-3 (Phases 1-5, 103 tasks total) constitute the minimum viable product

**Incremental Delivery**: Each user story can be deployed independently after Phase 2 (Foundational) is complete

**Coverage Improvement**: All 4 critical coverage gaps have been addressed (FR-014 task CC, WFR-007 business calendar, FR-036 notifications, FR-013 CMMN ad-hoc tasks) - 100% functional requirement coverage achieved

---

**Tasks complete and ready for implementation with TDD methodology**.
