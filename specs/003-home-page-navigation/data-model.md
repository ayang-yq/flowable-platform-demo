# Data Model: Home Page & Navigation

**Branch**: `003-home-page-navigation` | **Date**: 2026-03-22

## Overview

This feature introduces no new persisted entities. All data is computed from existing entities and Flowable engine services. The models below describe API response shapes and client-side state.

## API Response Models

### HomeSummary (Backend DTO)

Aggregated view returned by `GET /api/home/summary`.

| Field | Type | Description |
|-------|------|-------------|
| pendingTaskCount | long | Count of tasks assigned to the current user (uncompleted) |
| activeProcessCount | long | Count of active process instances started by the current user |
| recentActivity | List\<ActivityItem\> | Last 5 activity items (task completions, process starts) |

### ActivityItem (Backend DTO)

| Field | Type | Description |
|-------|------|-------------|
| id | String | Unique activity identifier (task ID or process instance ID) |
| type | String | Activity type: `TASK_COMPLETED`, `PROCESS_STARTED`, `TASK_ASSIGNED` |
| title | String | Human-readable title (task name or process definition name) |
| timestamp | LocalDateTime | When the activity occurred |
| processDefinitionKey | String | Associated process definition key (nullable) |

### UserInfo (Frontend client state)

Stored in React Context, populated from login response + localStorage.

| Field | Type | Description |
|-------|------|-------------|
| userId | string | UUID of the user |
| username | string | Login username |
| displayName | string | User's display name (falls back to username) |
| tenantCode | string | Current tenant code |
| tenantId | string | Current tenant UUID |
| roles | string[] | List of role names (e.g., `["ADMIN", "USER"]`) |

### NavigationItem (Frontend constant)

Static configuration for sidebar menu items.

| Field | Type | Description |
|-------|------|-------------|
| label | string | Display text (e.g., "Tasks") |
| path | string | Route path (e.g., "/tasks") |
| icon | string | Lucide icon name (e.g., "CheckSquare") |
| requiredRole | string? | Role required to see this item (null = visible to all) |

## Navigation Items Configuration

| Label | Path | Icon | Required Role |
|-------|------|------|---------------|
| Home | /home | Home | — |
| Tasks | /tasks | CheckSquare | — |
| Processes | /processes | GitBranch | — |
| Forms | /forms/builder | FileText | — |
| Dashboard | /dashboard | BarChart3 | — |
| Admin | /admin | Shield | ADMIN |

## Existing Entities Used (No Modifications)

- **User** (`users` table): `displayName`, `roles` relationship — read for profile display and role checking
- **Role** (`roles` table): `name` field — checked for ADMIN visibility
- **AuditLog** (`audit_logs` table): queried for recent activity feed
- Flowable engine tables: `ACT_RU_TASK` (pending tasks), `ACT_RU_EXECUTION` (active processes), `ACT_HI_TASKINST` / `ACT_HI_PROCINST` (history for activity feed)
