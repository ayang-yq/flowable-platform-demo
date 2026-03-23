# Research: Workspace Instance Management Dashboard

**Branch**: `004-workspace-instance-dashboard` | **Date**: 2026-03-23

## Decision 1: CMMN Case Instance Management

**Decision**: Use Flowable's native CmmnRuntimeService and CmmnHistoryService for case instance operations. Create a new CaseService alongside the existing ProcessService.

**Rationale**: The CMMN engine (v7.2.0) is already configured and auto-deploys .cmmn files from `classpath*:/processes/**/`. CmmnTaskService is already injected in ProcessController. CmmnRuntimeService and CmmnHistoryService are available via Spring auto-configuration but not yet used.

**Alternatives considered**:
- Wrapping CMMN operations in ProcessService → Rejected: violates single responsibility and makes the service overly complex
- Using Flowable REST API directly → Rejected: constitution mandates direct native service usage (Principle II)

## Decision 2: DMN Decision Execution

**Decision**: Use Flowable's native DmnRepositoryService for listing definitions and DmnRuleService for executing decisions. Create a new DecisionService.

**Rationale**: DMN engine (v7.2.0) is enabled with auto-deploy from `classpath*:/processes/**/`. A sample DMN model (`simple-decision.dmn`) already exists. The services are available via Spring auto-config but not yet autowired anywhere.

**Alternatives considered**:
- Using Flowable's REST DMN API → Rejected: constitution mandates direct native service usage
- Embedding DMN execution in ProcessService → Rejected: separate concern, different engine

## Decision 3: Start Form Handling

**Decision**: Use the existing custom FormService for start forms rather than Flowable's form engine.

**Rationale**: The project does NOT include `flowable-form-engine` dependency. Instead, it has a custom `FormService` backed by `FormSchema` entity with versioning support. Start forms will be linked to process/case definitions via a form key mapping stored in the custom form system.

**Alternatives considered**:
- Adding flowable-form-engine dependency → Rejected: would conflict with existing custom form implementation and SurveyJS-based form rendering
- No start form support → Rejected: spec requires FR-011 dynamic form rendering

## Decision 4: Dashboard Update Mechanism (Polling)

**Decision**: Use client-side polling with `setInterval` every 10-15 seconds for dashboard data refresh.

**Rationale**: Clarification session explicitly chose polling over WebSocket/SSE. This is simpler to implement and sufficient for dashboard freshness needs. No existing polling pattern in the codebase — this will be the first implementation.

**Constitution Violation Note**: Constitution constraint D states "Real-time updates use SSE or WebSockets - no polling." This is a justified deviation per user decision during specification clarification. Polling was chosen because:
1. Dashboard data freshness of 10-15 seconds is acceptable for this use case
2. Polling is significantly simpler than SSE/WebSocket infrastructure
3. Server load is minimal with paginated queries on indexed Flowable tables

**Alternatives considered**:
- WebSocket with STOMP → Rejected by user: over-engineered for this use case
- Server-Sent Events → Rejected by user: same complexity concern
- Manual refresh only → Rejected: poor UX for monitoring dashboard

## Decision 5: Unified Instance Dashboard Architecture

**Decision**: Create a unified workspace page with tabs (Active/Completed) that queries both BPMN and CMMN engines, merging results into a common InstanceDTO.

**Rationale**: The spec requires a unified view. Both engines use similar query patterns (runtime for active, history for completed). A common DTO avoids type-specific UI logic while preserving type information via a discriminator field.

**Alternatives considered**:
- Separate pages per engine type → Rejected: user expects unified workspace like Flowable Enterprise
- Single query via Flowable's unified API → Rejected: no single API spans BPMN+CMMN; must query both and merge

## Decision 6: Navigation Integration

**Decision**: Add a "Workspace" navigation item to the existing `navigation-items.ts` sidebar, positioned after "Home".

**Rationale**: The existing navigation uses a simple array in `frontend/src/components/layout/navigation-items.ts`. The workspace feature aligns with Flowable Enterprise's Workspace app concept. Route: `/workspace`.

**Alternatives considered**:
- Reuse existing `/processes` page → Rejected: workspace is broader (BPMN + CMMN + DMN) and has different UX
- Add as sub-items under existing nav → Rejected: workspace deserves top-level visibility

## Decision 7: Pagination Strategy

**Decision**: Server-side pagination using Spring Data-style `page`/`size` query parameters, consistent with existing admin pages.

**Rationale**: The existing admin pages (instances, users, tasks) already implement this pattern with `TaskPageResponse`-style DTOs containing `totalPages`, `totalElements`, `size`, `number`, `first`, `last`. Flowable's query APIs support `.listPage(offset, limit)` natively.

**Alternatives considered**:
- Client-side pagination → Rejected: constitution constraint D prohibits client-side pagination
- Cursor-based pagination → Rejected: Flowable APIs don't natively support cursors; offset-based is simpler and sufficient
