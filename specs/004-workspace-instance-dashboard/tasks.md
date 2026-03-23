# Tasks: Workspace Instance Management Dashboard

**Input**: Design documents from `/specs/004-workspace-instance-dashboard/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Unit tests (T014a-T014c) and integration test (T014d) added per Constitution Principle V (TDD).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create shared DTOs and enums used across all user stories

- [x] T001 [P] Create DefinitionType enum (BPMN, CMMN, DMN) in `backend/src/main/java/com/flowable/platform/dto/DefinitionType.java`
- [x] T002 [P] Create InstanceStatus enum (ACTIVE, SUSPENDED, COMPLETED, CANCELLED, FAILED) in `backend/src/main/java/com/flowable/platform/dto/InstanceStatus.java`
- [x] T003 [P] Create DefinitionDTO with fields: id, key, name, version, category, type (DefinitionType), hasStartForm, deploymentTime in `backend/src/main/java/com/flowable/platform/dto/DefinitionDTO.java`
- [x] T004 [P] Create InstanceDTO with fields: id, definitionId, definitionKey, definitionName, type, startTime, endTime, duration, startedBy, status, businessKey, tenantId in `backend/src/main/java/com/flowable/platform/dto/InstanceDTO.java`
- [x] T005 [P] Create InstanceDetailDTO extending InstanceDTO with variables, currentActivities, tasks in `backend/src/main/java/com/flowable/platform/dto/InstanceDetailDTO.java`
- [x] T006 [P] Create InstancePageDTO with content, totalElements, totalPages, size, number, first, last in `backend/src/main/java/com/flowable/platform/dto/InstancePageDTO.java`
- [x] T007 [P] Create DashboardSummaryDTO with activeCount, completedCount, startedTodayCount, myActiveCount in `backend/src/main/java/com/flowable/platform/dto/DashboardSummaryDTO.java`
- [x] T008 [P] Create StartInstanceRequest with variables and businessKey in `backend/src/main/java/com/flowable/platform/dto/StartInstanceRequest.java`
- [x] T009 [P] Create ExecuteDecisionRequest with inputVariables in `backend/src/main/java/com/flowable/platform/dto/ExecuteDecisionRequest.java`
- [x] T010 [P] Create DecisionExecutionDTO with decisionKey, decisionName, executionId, inputVariables, outputVariables, executionTime in `backend/src/main/java/com/flowable/platform/dto/DecisionExecutionDTO.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Backend services that MUST be complete before any user story frontend work can begin

**⚠️ CRITICAL**: No user story work can begin until this phase is complete. Tests (T014a-T014c) run parallel with services. T014d runs after T014 (needs controller).

- [x] T011 Create CaseService with CmmnRuntimeService and CmmnHistoryService injection. Implement: listCaseDefinitions (tenant-scoped via CmmnRepositoryService), startCaseInstance (with variables, businessKey, audit logging), listActiveCaseInstances (paginated, tenant-scoped), listCompletedCaseInstances (paginated, tenant-scoped, includes terminated), getCaseInstanceDetail (with variables, plan items). Follow ProcessService pattern using `MultiTenantFilter.getCurrentTenantId()` in `backend/src/main/java/com/flowable/platform/service/CaseService.java`
- [x] T012 Create DecisionService with DmnRepositoryService and DmnRuleService injection. Implement: listDecisionDefinitions (tenant-scoped), executeDecision (evaluate decision table with input variables, return output, audit log), getDecisionExecutionHistory (paginated). Follow ProcessService pattern in `backend/src/main/java/com/flowable/platform/service/DecisionService.java`
- [x] T013 Create WorkspaceService that injects ProcessService, CaseService, DecisionService. Implement: getAllDefinitions (merge BPMN from RepositoryService + CMMN from CmmnRepositoryService + DMN from DmnRepositoryService into List<DefinitionDTO>, filterable by type), getActiveInstances (merge BPMN runtime + CMMN runtime into InstancePageDTO with pagination, search, type filter, startedBy filter, date range), getCompletedInstances (merge BPMN history + CMMN history + DMN history into InstancePageDTO with status filter for Completed/Cancelled/Failed), getInstanceDetail (route to correct engine by type param, return InstanceDetailDTO), getDashboardSummary (count active + completed + startedToday + myActive across both engines) in `backend/src/main/java/com/flowable/platform/service/WorkspaceService.java`
- [x] T014 Create WorkspaceController with all 8 REST endpoints per contracts/workspace-api.md: GET /api/workspace/definitions, POST /api/workspace/processes/{definitionKey}/start, POST /api/workspace/cases/{definitionKey}/start, POST /api/workspace/decisions/{definitionKey}/execute, GET /api/workspace/instances/active, GET /api/workspace/instances/completed, GET /api/workspace/instances/{id}, GET /api/workspace/summary. All endpoints return ApiResponse<T> wrappers, require JWT auth and X-Tenant-Id header in `backend/src/main/java/com/flowable/platform/controller/WorkspaceController.java`
- [x] T014a [P] Write unit tests for CaseService: test listCaseDefinitions (tenant-scoped), startCaseInstance (happy path + missing definition), listActiveCaseInstances (pagination, filters), listCompletedCaseInstances (includes terminated). Use Mockito to mock CmmnRuntimeService, CmmnHistoryService, CmmnRepositoryService in `backend/src/test/java/com/flowable/platform/test/unit/CaseServiceTest.java`
- [x] T014b [P] Write unit tests for DecisionService: test listDecisionDefinitions (tenant-scoped), executeDecision (happy path + invalid input), getDecisionExecutionHistory (pagination). Use Mockito to mock DmnRepositoryService, DmnRuleService in `backend/src/test/java/com/flowable/platform/test/unit/DecisionServiceTest.java`
- [x] T014c [P] Write unit tests for WorkspaceService: test getAllDefinitions (merges BPMN+CMMN+DMN, type filter), getActiveInstances (merged pagination, search, startedBy filter), getCompletedInstances (status filter for Completed/Cancelled/Failed), getDashboardSummary (correct counts). Use Mockito to mock ProcessService, CaseService, DecisionService in `backend/src/test/java/com/flowable/platform/test/unit/WorkspaceServiceTest.java`
- [x] T014d Write integration test for WorkspaceController: test all 8 endpoints with REST Assured against a running Flowable engine via Testcontainers. Cover: list definitions returns BPMN+CMMN+DMN, start process returns InstanceDTO, start case returns InstanceDTO, execute decision returns output, active instances paginated, completed instances include status badges, instance detail returns variables, summary counts are accurate. Extend AbstractIntegrationTest in `backend/src/test/java/com/flowable/platform/test/integration/WorkspaceControllerIntegrationTest.java`
- [x] T015 Add "Workspace" navigation item after "Home" in the navigation array with path `/workspace` and Briefcase icon from lucide-react in `frontend/src/components/layout/navigation-items.ts`

**Checkpoint**: Backend API fully operational, navigation updated. User story frontend work can now begin.

---

## Phase 3: User Story 1 - Start a BPMN Process Instance (Priority: P1) 🎯 MVP

**Goal**: Users can browse available BPMN process definitions and start a new process instance from the workspace start page

**Independent Test**: Deploy a sample BPMN model, navigate to /workspace/start, select the process, click Start, verify instance appears in active list

### Implementation for User Story 1

- [x] T016 [P] [US1] Create TypeBadge component that renders colored badges for BPMN (blue), CMMN (green), DMN (orange) using Tailwind CSS classes and Lucide icons (GitBranch for BPMN, Layers for CMMN, Table2 for DMN) in `frontend/src/components/workspace/TypeBadge.tsx`
- [x] T017 [P] [US1] Create StatusBadge component that renders status badges: ACTIVE (green), SUSPENDED (yellow), COMPLETED (blue), CANCELLED (gray), FAILED (red) using Tailwind CSS classes in `frontend/src/components/workspace/StatusBadge.tsx`
- [x] T018 [P] [US1] Create DefinitionCard component displaying definition name, key, version, category, type badge (using TypeBadge), and a "Start" button. Card layout with hover effect. Accept onClick handler prop in `frontend/src/components/workspace/DefinitionCard.tsx`
- [x] T019 [US1] Create the Start Instance page as a client component ('use client'). Fetch definitions from GET /api/workspace/definitions. Display grid of DefinitionCard components. Filter by type tabs (All/BPMN/CMMN/DMN). When user clicks Start on a BPMN definition without a form, POST to /api/workspace/processes/{key}/start with empty variables, show success toast, redirect to /workspace. When definition has a start form key OR user wants to pass variables, show a modal with dynamic key-value variable input (add/remove rows, type selection: string/number/boolean). Validate required fields client-side. POST variables to start endpoint. Handle errors (definition not found, validation). Note: full SurveyJS/FormService integration deferred per FR-011 scope in `frontend/src/app/(authenticated)/workspace/start/page.tsx`

**Checkpoint**: User can browse BPMN definitions and start process instances from /workspace/start

---

## Phase 4: User Story 2 - Start a CMMN Case Instance (Priority: P1)

**Goal**: Users can start CMMN case instances alongside BPMN processes from the same start page

**Independent Test**: Deploy a sample CMMN model, navigate to /workspace/start, verify CMMN definitions appear with distinct type badge, start a case instance

### Implementation for User Story 2

- [x] T020 [US2] Extend the Start Instance page to handle CMMN definitions: when user clicks Start on a CMMN definition, POST to /api/workspace/cases/{key}/start. CMMN definitions should already appear via the GET /api/workspace/definitions endpoint (handled by WorkspaceService). Ensure TypeBadge distinguishes CMMN from BPMN. Handle empty state when no CMMN definitions exist (show helpful message) in `frontend/src/app/(authenticated)/workspace/start/page.tsx`

**Checkpoint**: User can start both BPMN process instances and CMMN case instances from /workspace/start

---

## Phase 5: User Story 3 - Active Instances Dashboard (Priority: P1)

**Goal**: Users see a paginated dashboard of all active BPMN and CMMN instances with search, filter, and "My Instances" toggle

**Independent Test**: Start several BPMN/CMMN instances, navigate to /workspace, verify all appear in active tab with correct type badges, pagination works, My Instances filter works

### Implementation for User Story 3

- [x] T021 [P] [US3] Create InstanceTable component as a client component. Accept props: instances (InstanceDTO[]), pagination metadata, onPageChange, loading state. Render table with columns: Name/Key, Type (TypeBadge), Status (StatusBadge), Started By, Start Time (formatted with date-fns). Include pagination controls (Previous/Next buttons, page info) following existing admin page pattern. Row click navigates to /workspace/{id}?type={type} in `frontend/src/components/workspace/InstanceTable.tsx`
- [x] T022 [US3] Create the main Workspace dashboard page as a client component ('use client'). Implement Active tab as default view: fetch from GET /api/workspace/instances/active with page/size params. Include search input (filters by name/businessKey), type filter dropdown (All/BPMN/CMMN), date range picker, and "My Instances" toggle (passes current username as startedBy param). Use InstanceTable component for rendering. Add 12-second polling with useEffect + setInterval that re-fetches active instances. Include cleanup on unmount. Add "Start New" button linking to /workspace/start. Show loading skeleton on initial load in `frontend/src/app/(authenticated)/workspace/page.tsx`

**Checkpoint**: Active instances dashboard is fully functional with search, filter, pagination, polling, and My Instances toggle

---

## Phase 6: User Story 4 - Completed Instances Dashboard (Priority: P2)

**Goal**: Users can view completed, cancelled, and failed instances with status badges and duration info

**Independent Test**: Complete/cancel some instances, switch to Completed tab, verify instances appear with correct status badges and duration

### Implementation for User Story 4

- [x] T023 [US4] Add Completed tab to the Workspace dashboard page using Radix UI Tabs component. Completed tab fetches from GET /api/workspace/instances/completed with same pagination/filter params plus additional status filter (All/Completed/Cancelled/Failed). Reuse InstanceTable component — add endTime and duration columns (format duration with date-fns: "2h 15m", "3d 4h", etc). Apply polling to whichever tab is active. Tab state preserved in URL query param in `frontend/src/app/(authenticated)/workspace/page.tsx`
- [x] T024 [US4] Create Instance Detail page. Fetch from GET /api/workspace/instances/{id}?type={type}. Display: instance header (name, type badge, status badge), metadata card (started by, start time, end time, duration, business key), variables table (key-value display of process/case variables), current activities list (for active instances), active tasks list with assignee info (for active BPMN/CMMN instances). Back button returns to workspace. Handle both active and completed instances in `frontend/src/app/(authenticated)/workspace/[id]/page.tsx`

**Checkpoint**: Both Active and Completed tabs work, instance detail view shows full information

---

## Phase 7: User Story 5 - Execute a DMN Decision (Priority: P2)

**Goal**: Users can execute DMN decisions and view results, with executions appearing in completed history

**Independent Test**: Deploy simple-decision.dmn, navigate to /workspace/start, select DMN definition, provide input values, execute, verify output displayed and execution appears in completed tab

### Implementation for User Story 5

- [x] T025 [US5] Extend the Start Instance page to handle DMN definitions: when user clicks on a DMN definition, show a form for inputting decision variables (dynamic key-value pairs with type selection: string, number, boolean). On submit, POST to /api/workspace/decisions/{key}/execute. Display the decision output in a results panel (table showing output variable names and values). Add "Execute Another" button to re-run with different inputs. Handle validation errors in `frontend/src/app/(authenticated)/workspace/start/page.tsx`
- [x] T026 [US5] Ensure DMN execution history appears in the Completed tab by verifying WorkspaceService.getCompletedInstances includes DMN executions with type=DMN. Verify DecisionService returns history records that map to InstanceDTO format. DMN entries in InstanceTable should show execution time as startTime, input/output as detail, and type badge as DMN in `frontend/src/app/(authenticated)/workspace/page.tsx`

**Checkpoint**: Full BPMN + CMMN + DMN support in workspace start page, DMN history in completed tab

---

## Phase 8: User Story 6 - Dashboard Summary Statistics (Priority: P3)

**Goal**: Summary cards at the top of the dashboard showing active count, completed count, started today, and user's active count

**Independent Test**: Verify summary card counts match actual instance counts in the system

### Implementation for User Story 6

- [x] T027 [P] [US6] Create SummaryCards component as a client component. Accept DashboardSummaryDTO props. Render 4 cards in a responsive grid (grid-cols-1 sm:grid-cols-2 lg:grid-cols-4): Active Instances (with Activity icon), Completed Instances (with CheckCircle icon), Started Today (with Clock icon), My Active (with User icon). Each card shows count with label. Show "Get Started" message with link to /workspace/start when all counts are zero. Include loading skeleton state in `frontend/src/components/workspace/SummaryCards.tsx`
- [x] T028 [US6] Integrate SummaryCards into the Workspace dashboard page. Fetch from GET /api/workspace/summary on mount and include in polling interval. Place above the tabs. Summary counts update every polling cycle in `frontend/src/app/(authenticated)/workspace/page.tsx`

**Checkpoint**: Dashboard shows summary statistics that update with polling

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T028a [P] Add form key detection to definition listing: in WorkspaceService.getAllDefinitions, check ProcessDefinition.hasStartFormKey() for BPMN and CaseDefinition equivalent for CMMN. Populate DefinitionDTO.hasStartForm field accordingly. When hasStartForm is true, the frontend can show a form indicator on the DefinitionCard. This does NOT integrate with the existing custom FormService (deferred) — it only sets the boolean flag from Flowable's native definition metadata in `backend/src/main/java/com/flowable/platform/service/WorkspaceService.java`
- [x] T029 Add error boundary and error handling for all workspace pages — handle API errors gracefully with user-friendly messages, handle definition-not-found when starting stale definitions in `frontend/src/app/(authenticated)/workspace/page.tsx` and `frontend/src/app/(authenticated)/workspace/start/page.tsx`
- [x] T030 [P] Add empty states for all views: no definitions available (with link to admin deploy page), no active instances ("Get Started" CTA), no completed instances in `frontend/src/app/(authenticated)/workspace/page.tsx` and `frontend/src/app/(authenticated)/workspace/start/page.tsx`
- [x] T031 [P] Add audit logging calls in CaseService (CASE_STARTED) and DecisionService (DECISION_EXECUTED) using existing AuditService.logAction() pattern from ProcessService in `backend/src/main/java/com/flowable/platform/service/CaseService.java` and `backend/src/main/java/com/flowable/platform/service/DecisionService.java`
- [x] T032 Verify all WorkspaceService queries include tenant_id filtering via MultiTenantFilter.getCurrentTenantId() — audit every query method to ensure no cross-tenant data leakage in `backend/src/main/java/com/flowable/platform/service/WorkspaceService.java`
- [x] T033 Add SecurityConfig updates to permit /api/workspace/** endpoints for authenticated users only in `backend/src/main/java/com/flowable/platform/config/SecurityConfig.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately. All tasks are [P] parallelizable.
- **Foundational (Phase 2)**: Depends on Phase 1 DTOs. T011-T013 can run in parallel (different service files). T014 depends on T011-T013 (needs services). T015 is independent.
- **User Stories (Phase 3-8)**: All depend on Phase 2 completion (backend API must be operational).
  - US1 (Phase 3): Can start after Phase 2
  - US2 (Phase 4): Depends on US1 (extends same page)
  - US3 (Phase 5): Can start after Phase 2, independent of US1/US2
  - US4 (Phase 6): Depends on US3 (adds tabs to same page)
  - US5 (Phase 7): Depends on US1 (extends start page)
  - US6 (Phase 8): Depends on US3 (adds to dashboard page)
- **Polish (Phase 9)**: Depends on all user stories being complete

### User Story Dependencies

- **US1 (Start BPMN)**: Phase 2 only — first story, no other dependencies
- **US2 (Start CMMN)**: US1 — extends the same start page
- **US3 (Active Dashboard)**: Phase 2 only — independent of US1/US2 (separate page)
- **US4 (Completed Dashboard)**: US3 — adds Completed tab to same dashboard page
- **US5 (DMN Execution)**: US1 — extends the same start page
- **US6 (Summary Stats)**: US3 — adds summary cards to dashboard page

### Within Each User Story

- Frontend components marked [P] can be built in parallel
- Page-level tasks depend on their component tasks
- Integration tasks come after page tasks

### Parallel Opportunities

- **Phase 1**: All 10 DTO tasks (T001-T010) can run in parallel
- **Phase 2**: T011, T012 can run in parallel (different service files). T015 independent of all.
- **Phase 3+**: US1 and US3 can run in parallel (different pages). Within US3, T021 components can run in parallel with T022 page.
- **Phase 9**: T030, T031 can run in parallel

---

## Parallel Example: Phase 1 (Setup)

```bash
# Launch all DTOs in parallel (10 independent files):
Task T001: "Create DefinitionType enum"
Task T002: "Create InstanceStatus enum"
Task T003: "Create DefinitionDTO"
Task T004: "Create InstanceDTO"
Task T005: "Create InstanceDetailDTO"
Task T006: "Create InstancePageDTO"
Task T007: "Create DashboardSummaryDTO"
Task T008: "Create StartInstanceRequest"
Task T009: "Create ExecuteDecisionRequest"
Task T010: "Create DecisionExecutionDTO"
```

## Parallel Example: Phase 2 (Foundational Services)

```bash
# Launch services in parallel (3 independent files):
Task T011: "Create CaseService"
Task T012: "Create DecisionService"
# Wait for T011+T012, then:
Task T013: "Create WorkspaceService"
# Wait for T013, then:
Task T014: "Create WorkspaceController"
# T015 (nav update) can run anytime in parallel
```

## Parallel Example: US1 + US3 in Parallel

```bash
# These target different pages and can run simultaneously:
# Developer A: US1 (Start page)
Task T016-T019: Start Instance page components and page

# Developer B: US3 (Dashboard page)
Task T021-T022: Instance table component and dashboard page
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 3 Only)

1. Complete Phase 1: Setup (10 DTOs, all parallel)
2. Complete Phase 2: Foundational (services + controller + nav)
3. Complete Phase 3: US1 - Start BPMN Process Instance
4. Complete Phase 5: US3 - Active Instances Dashboard
5. **STOP and VALIDATE**: User can start a BPMN process and see it in the active dashboard
6. Deploy/demo if ready

### Incremental Delivery

1. Setup + Foundational → Backend API ready
2. US1 (Start BPMN) → Users can start processes (MVP!)
3. US2 (Start CMMN) → CMMN support added to start page
4. US3 (Active Dashboard) → Monitor active instances
5. US4 (Completed Dashboard) → Historical view with status badges
6. US5 (DMN Execution) → Decision support complete
7. US6 (Summary Stats) → Dashboard polish
8. Polish → Error handling, audit, security hardening

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- No new database migrations needed — all data from Flowable-managed tables
- Follow existing patterns: ProcessService for backend services, admin/instances for frontend pagination
- Polling uses 12-second interval (within 10-15s spec requirement)
- All Flowable service injection is via Spring auto-configuration (already configured in application.yml)
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
