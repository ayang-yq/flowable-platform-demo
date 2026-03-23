# Quickstart: Workspace Instance Management Dashboard

**Branch**: `004-workspace-instance-dashboard` | **Date**: 2026-03-23

## Prerequisites

- Java 21, Node.js 18+, PostgreSQL 15+ running
- Backend and frontend from features 001-003 are working
- Sample BPMN/CMMN/DMN models deployed (auto-deployed from `backend/src/main/resources/processes/`)

## What This Feature Adds

### Backend (Spring Boot)
New files in `com.flowable.platform`:

| Component | Path | Purpose |
|-----------|------|---------|
| WorkspaceController | controller/ | REST endpoints under `/api/workspace` |
| CaseService | service/ | CMMN case operations via CmmnRuntimeService/CmmnHistoryService |
| DecisionService | service/ | DMN execution via DmnRepositoryService/DmnRuleService |
| WorkspaceService | service/ | Unified workspace queries (merge BPMN+CMMN results) |
| DefinitionDTO | dto/ | Unified definition response (BPMN/CMMN/DMN) |
| InstanceDTO | dto/ | Unified instance response |
| InstanceDetailDTO | dto/ | Extended instance with variables/tasks |
| InstancePageDTO | dto/ | Paginated instance list response |
| DashboardSummaryDTO | dto/ | Dashboard summary statistics |
| DecisionExecutionDTO | dto/ | DMN execution result |
| StartInstanceRequest | dto/ | Request body for starting instances |
| ExecuteDecisionRequest | dto/ | Request body for DMN execution |

### Frontend (Next.js)
New files in `frontend/src`:

| Component | Path | Purpose |
|-----------|------|---------|
| Workspace page | app/(authenticated)/workspace/page.tsx | Main workspace dashboard |
| Instance detail page | app/(authenticated)/workspace/[id]/page.tsx | Instance detail view |
| Start instance page | app/(authenticated)/workspace/start/page.tsx | Start instance / execute decision |
| DefinitionCard | components/workspace/DefinitionCard.tsx | Card for a BPMN/CMMN/DMN definition |
| InstanceTable | components/workspace/InstanceTable.tsx | Paginated table of instances |
| SummaryCards | components/workspace/SummaryCards.tsx | Dashboard summary statistics |
| StatusBadge | components/workspace/StatusBadge.tsx | Instance status badge (Active/Completed/Cancelled/Failed) |
| TypeBadge | components/workspace/TypeBadge.tsx | Definition/instance type badge (BPMN/CMMN/DMN) |
| Navigation update | components/layout/navigation-items.ts | Add "Workspace" nav item |

### No Database Changes
No Flyway migrations needed — uses Flowable's managed tables only.

## Development Flow

1. **Backend first**: Create DTOs → Services (CaseService, DecisionService, WorkspaceService) → WorkspaceController
2. **Frontend second**: Add navigation → Build workspace page → Build start page → Build detail page
3. **Integration**: Wire frontend to backend API, add polling

## Key Patterns to Follow

- **Services**: Follow `ProcessService` pattern — inject Flowable native services, use `MultiTenantFilter.getCurrentTenantId()` for tenant isolation
- **Controller**: Follow `ProcessController` pattern — return `ApiResponse<T>` wrappers
- **Frontend pages**: Follow `admin/instances/page.tsx` pattern for server-rendered paginated lists
- **Polling**: Use `useEffect` + `setInterval(fetchData, 12000)` with cleanup

## Testing

```bash
# Run backend tests
cd backend && mvn test

# Run frontend tests
cd frontend && npm test
```
