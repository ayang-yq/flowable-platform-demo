# Implementation Plan: Workspace Instance Management Dashboard

**Branch**: `004-workspace-instance-dashboard` | **Date**: 2026-03-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-workspace-instance-dashboard/spec.md`

## Summary

Build a workspace feature that enables users to start BPMN process instances, CMMN case instances, and execute DMN decisions from a unified interface. Includes a paginated dashboard with Active/Completed tabs showing all tenant instances, summary statistics, search/filter capabilities, and 10-15 second polling refresh. Backend uses Flowable 7.2.0 native services (RuntimeService, CmmnRuntimeService, CmmnHistoryService, DmnRuleService). Frontend adds a new `/workspace` route with Next.js App Router pages.

## Technical Context

**Language/Version**: Java 21, TypeScript 5+
**Primary Dependencies**: Spring Boot 3.5.x, Flowable 7.2.0 (BPMN + CMMN + DMN engines), Next.js 14 (App Router), React 18+, Tailwind CSS, Lucide React, Radix UI
**Storage**: PostgreSQL 15+ (multi-tenant, Flowable-managed tables only — no new migrations)
**Testing**: JUnit 5, Testcontainers, Jest, React Testing Library
**Target Platform**: Linux server (backend), Modern browsers (frontend)
**Project Type**: Web service (multi-tenant workflow platform)
**Performance Goals**: <500ms p95 response time, dashboard loads <2s for 1000 instances
**Constraints**: Multi-tenant data isolation, Flowable-native services, Server/Client Component separation
**Scale/Scope**: Multi-tenant SaaS, 10k+ users, 50+ process/case/decision definitions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] **Multi-Tenant Isolation**: All queries use `MultiTenantFilter.getCurrentTenantId()` and Flowable tenant-scoped queries. TFR-001/002/003 enforced.
- [x] **Flowable-Native**: Direct use of RuntimeService, HistoryService, CmmnRuntimeService, CmmnHistoryService, DmnRepositoryService, DmnRuleService. No abstraction layers.
- [x] **Server/Client Boundaries**: Data fetching in server-rendered pages. Polling uses Client Components calling API routes. No Flowable calls from client.
- [x] **Form-Process Binding**: Start forms use existing custom FormService (SurveyJS). Form variables map to Flowable process/case variables via camelCase naming.
- [x] **Test Coverage**: Integration tests planned for all service methods (start instance, query, execute decision). Unit tests for DTOs and controllers.
- [x] **Audit Trail**: All workspace actions (start process, start case, execute decision) logged via existing AuditService with userId, tenantId, timestamp, IP.
- [x] **Performance**: All Flowable queries include tenantId filtering. Paginated responses with server-side offset/limit.
- [x] **Security**: RBAC at service layer via Spring Security. Process variable encryption for sensitive data uses existing patterns.

## Constitution Check (Post-Design)

- [x] **Multi-Tenant Isolation**: Confirmed — WorkspaceService, CaseService, DecisionService all filter by tenant.
- [x] **Flowable-Native**: Confirmed — no wrapper patterns, direct service injection.
- [x] **Server/Client Boundaries**: Confirmed — workspace pages server-render initial data, polling uses client `useEffect`.
- [x] **Form-Process Binding**: Confirmed — existing FormService pattern reused for start forms.
- [x] **Test Coverage**: Confirmed — test plan covers service + controller layers.
- [x] **Audit Trail**: Confirmed — AuditService.logAction() called for all workspace operations.
- [x] **Performance**: Confirmed — paginated queries, tenant-scoped, indexed Flowable tables.
- [x] **Security**: Confirmed — JWT + tenant header required on all endpoints.

## Project Structure

### Documentation (this feature)

```text
specs/004-workspace-instance-dashboard/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: Research decisions
├── data-model.md        # Phase 1: Data model and DTOs
├── quickstart.md        # Phase 1: Development quickstart
├── contracts/
│   └── workspace-api.md # Phase 1: REST API contract
├── checklists/
│   └── requirements.md  # Specification quality checklist
└── tasks.md             # Phase 2: Task breakdown (via /speckit.tasks)
```

### Source Code (repository root)

```text
backend/
├── src/main/java/com/flowable/platform/
│   ├── controller/
│   │   └── WorkspaceController.java        # REST endpoints for /api/workspace
│   ├── dto/
│   │   ├── DefinitionDTO.java              # Unified definition (BPMN/CMMN/DMN)
│   │   ├── InstanceDTO.java                # Unified instance response
│   │   ├── InstanceDetailDTO.java          # Extended instance with variables/tasks
│   │   ├── InstancePageDTO.java            # Paginated response wrapper
│   │   ├── DashboardSummaryDTO.java        # Summary statistics
│   │   ├── DecisionExecutionDTO.java       # DMN execution result
│   │   ├── StartInstanceRequest.java       # Start process/case request body
│   │   └── ExecuteDecisionRequest.java     # DMN execution request body
│   └── service/
│       ├── CaseService.java                # CMMN operations (CmmnRuntimeService/CmmnHistoryService)
│       ├── DecisionService.java            # DMN operations (DmnRepositoryService/DmnRuleService)
│       └── WorkspaceService.java           # Unified queries (merges BPMN+CMMN results)
└── src/test/java/com/flowable/platform/test/
    ├── unit/
    │   ├── WorkspaceServiceTest.java
    │   ├── CaseServiceTest.java
    │   └── DecisionServiceTest.java
    └── integration/
        └── WorkspaceControllerIntegrationTest.java

frontend/
├── src/
│   ├── app/(authenticated)/workspace/
│   │   ├── page.tsx                        # Main workspace dashboard (Active/Completed tabs)
│   │   ├── start/page.tsx                  # Start instance / execute decision page
│   │   └── [id]/page.tsx                   # Instance detail view
│   ├── components/workspace/
│   │   ├── DefinitionCard.tsx              # Definition card with type badge
│   │   ├── InstanceTable.tsx               # Paginated instance table
│   │   ├── SummaryCards.tsx                # Dashboard summary statistics
│   │   ├── StatusBadge.tsx                 # Instance status badge
│   │   └── TypeBadge.tsx                   # BPMN/CMMN/DMN type badge
│   └── components/layout/
│       └── navigation-items.ts             # Updated: add "Workspace" item
└── src/test/
    └── components/
        └── workspace/
            └── InstanceTable.test.tsx       # Table component tests
```

**Structure Decision**: Web application structure (existing backend/frontend split). New code follows established patterns — backend services in `service/`, controllers in `controller/`, DTOs in `dto/`. Frontend uses App Router with `(authenticated)` route group.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Polling instead of SSE/WebSocket (Constitution D) | User explicitly chose polling during clarification for simplicity | SSE/WebSocket adds infrastructure complexity for minimal benefit on a monitoring dashboard with 10-15s acceptable delay |
