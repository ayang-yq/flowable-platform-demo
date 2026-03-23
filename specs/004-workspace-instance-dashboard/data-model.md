# Data Model: Workspace Instance Management Dashboard

**Branch**: `004-workspace-instance-dashboard` | **Date**: 2026-03-23

## Overview

This feature primarily reads from Flowable's managed tables (no new database tables). The data model defines DTOs and query contracts for interacting with Flowable's BPMN, CMMN, and DMN engines.

## Entities (Flowable-Managed, Read-Only)

### Process Definition (BPMN)
**Source**: Flowable `ACT_RE_PROCDEF` table via `RepositoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique definition ID (e.g., "myProcess:1:12345") |
| key | String | Process definition key |
| name | String | Human-readable name |
| version | Integer | Version number |
| category | String | Category for grouping |
| deploymentId | String | Associated deployment |
| tenantId | String | Tenant identifier |
| hasStartFormKey | Boolean | Whether a start form is defined |

### Case Definition (CMMN)
**Source**: Flowable `ACT_CMMN_RE_CASEDEF` table via `CmmnRepositoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique definition ID |
| key | String | Case definition key |
| name | String | Human-readable name |
| version | Integer | Version number |
| category | String | Category for grouping |
| deploymentId | String | Associated deployment |
| tenantId | String | Tenant identifier |
| hasStartFormKey | Boolean | Whether a start form is defined |

### Decision Definition (DMN)
**Source**: Flowable `ACT_DMN_DECISION` table via `DmnRepositoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique definition ID |
| key | String | Decision definition key |
| name | String | Human-readable name |
| version | Integer | Version number |
| category | String | Category for grouping |
| deploymentId | String | Associated deployment |
| tenantId | String | Tenant identifier |
| decisionType | String | Type of decision (table, service) |

### Process Instance (BPMN Runtime)
**Source**: Flowable `ACT_RU_EXECUTION` via `RuntimeService` / `ACT_HI_PROCINST` via `HistoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Process instance ID |
| processDefinitionId | String | Definition reference |
| processDefinitionKey | String | Definition key |
| processDefinitionName | String | Definition name |
| businessKey | String | Business key |
| startTime | Date | When the instance was started |
| endTime | Date | When the instance ended (null if active) |
| startUserId | String | User who started the instance |
| tenantId | String | Tenant identifier |
| state | String | Current state (active, suspended, completed, cancelled, failed) |
| variables | Map | Process variables |

### Case Instance (CMMN Runtime)
**Source**: Flowable `ACT_CMMN_RU_CASE_INST` via `CmmnRuntimeService` / `ACT_CMMN_HI_CASE_INST` via `CmmnHistoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Case instance ID |
| caseDefinitionId | String | Definition reference |
| caseDefinitionKey | String | Definition key |
| caseDefinitionName | String | Definition name |
| businessKey | String | Business key |
| startTime | Date | When the case was started |
| endTime | Date | When the case ended (null if active) |
| startUserId | String | User who started the case |
| tenantId | String | Tenant identifier |
| state | String | Current state (active, completed, terminated) |
| variables | Map | Case variables |

### Decision Execution (DMN History)
**Source**: Flowable `ACT_DMN_HI_DECISION_EXECUTION` via `DmnHistoryService`

| Field | Type | Description |
|-------|------|-------------|
| id | String | Execution ID |
| decisionDefinitionId | String | Definition reference |
| decisionKey | String | Decision key |
| decisionName | String | Decision name |
| executionTime | Date | When the decision was evaluated |
| executedBy | String | User who executed |
| tenantId | String | Tenant identifier |
| inputVariables | Map | Input values provided |
| outputVariables | Map | Decision output results |
| failed | Boolean | Whether execution failed |

## Application DTOs

### DefinitionDTO (Unified)
Combines BPMN, CMMN, and DMN definitions into a single response type.

| Field | Type | Description |
|-------|------|-------------|
| id | String | Definition ID |
| key | String | Definition key |
| name | String | Human-readable name |
| version | Integer | Version number |
| category | String | Category for grouping |
| type | Enum | BPMN, CMMN, or DMN |
| hasStartForm | Boolean | Whether a start form exists (false for DMN) |
| deploymentTime | LocalDateTime | When the definition was deployed |

### InstanceDTO (Unified)
Combines BPMN process instances, CMMN case instances, and DMN executions.

| Field | Type | Description |
|-------|------|-------------|
| id | String | Instance/execution ID |
| definitionId | String | Definition reference |
| definitionKey | String | Definition key |
| definitionName | String | Definition name |
| type | Enum | BPMN, CMMN, or DMN |
| startTime | LocalDateTime | When started/executed |
| endTime | LocalDateTime | When ended (null if active) |
| duration | Long | Duration in milliseconds (null if active) |
| startedBy | String | User who started |
| status | Enum | ACTIVE, SUSPENDED, COMPLETED, CANCELLED, FAILED |
| businessKey | String | Business key (null for DMN) |
| tenantId | String | Tenant identifier |

### InstanceDetailDTO (Extended)
Extends InstanceDTO with additional detail fields.

| Field | Type | Extends |
|-------|------|---------|
| (all InstanceDTO fields) | | InstanceDTO |
| variables | Map<String, Object> | Process/case variables or DMN input/output |
| currentActivities | List<String> | Active activity names (BPMN/CMMN only) |
| tasks | List<TaskDTO> | Active tasks (BPMN/CMMN only) |

### InstancePageDTO (Paginated Response)
Server-side paginated response for instance lists.

| Field | Type | Description |
|-------|------|-------------|
| content | List<InstanceDTO> | Page of instances |
| totalElements | Long | Total matching instances |
| totalPages | Integer | Total pages |
| size | Integer | Page size |
| number | Integer | Current page number (0-based) |
| first | Boolean | Is first page |
| last | Boolean | Is last page |

### DashboardSummaryDTO
Summary statistics for the dashboard header.

| Field | Type | Description |
|-------|------|-------------|
| activeCount | Long | Total active instances (BPMN + CMMN) |
| completedCount | Long | Total completed instances |
| startedTodayCount | Long | Instances started today |
| myActiveCount | Long | Current user's active instances |

## State Transitions

### BPMN Process Instance States
```
ACTIVE → SUSPENDED → ACTIVE (resume)
ACTIVE → COMPLETED (normal end)
ACTIVE → CANCELLED (terminated by user)
ACTIVE → FAILED (error boundary)
SUSPENDED → CANCELLED (terminated while suspended)
```

### CMMN Case Instance States
```
ACTIVE → COMPLETED (all required plan items complete)
ACTIVE → TERMINATED (manually terminated)
ACTIVE → FAILED (error)
```

### DMN Decision Execution
```
(no state transitions — single execution, recorded as history)
EXECUTED → (done, with success/failure flag)
```

## No New Database Tables

This feature uses only Flowable's managed tables. No Flyway migrations are needed.

The existing `audit_log` table will be used for recording workspace actions (start instance, execute decision) via the existing `AuditService`.
