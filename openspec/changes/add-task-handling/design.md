## Context

The platform has a complete backend for task management (TaskController, TaskManagementService) with endpoints for claiming, completing, delegating, and reassigning tasks. The frontend has task list pages and a TaskCard component with action buttons, but those buttons are not wired to any API calls. There is no task detail page, no task completion form, and the API client lacks task-specific methods.

## Goals / Non-Goals

**Goals:**
- Task detail page at `/tasks/[id]` with full task metadata, form rendering, and action buttons
- Working claim/complete/delegate actions connected to backend API
- API client methods for all task operations
- Instance detail page shows active tasks with inline claim/complete actions
- Task completion with form variables when a form schema exists

**Non-Goals:**
- Real-time task notifications (WebSocket)
- Task comments or collaboration features
- Bulk task operations
- Task history/timeline view
- Admin task reassignment UI (admin API exists but UI is deferred)

## Decisions

### 1. Task Detail Page Structure
**Decision:** Single page with sidebar metadata + main content area for form

**Rationale:** Follows the instance detail page pattern already established in the codebase. Keeps task metadata always visible while the form scrolls independently.

### 2. Task Completion Flow
**Decision:** Check for form schema via task definition key, render form if available, otherwise show a simple complete confirmation

**Rationale:** Some tasks need variable input (form fields) while others can be completed directly. The existing `form_schemas` table links schemas to `process_definition_key` + `task_definition_key`.

### 3. Task Actions in TaskCard
**Decision:** Wire existing buttons directly to API calls with optimistic UI updates

**Rationale:** Buttons already exist in the UI. Just need to connect them. Optimistic updates keep the UI responsive without waiting for server round-trips.

### 4. Instance Detail Task Integration
**Decision:** Add claim/complete buttons to the existing "Active Tasks" section on instance detail page

**Rationale:** Users viewing an instance should be able to act on tasks without navigating away.

### 5. Navigation After Task Action
**Decision:** After completing a task, redirect to the parent instance detail page. After claiming, refresh the task list.

**Rationale:** Completing a task typically means the instance has progressed, so showing the updated instance is useful. Claiming keeps the user in the task context.

## Risks / Trade-offs

### Form Schema Availability
**Risk:** Task may reference a form schema that doesn't exist in the database

**Mitigation:** Show a fallback "no form required" message and allow direct completion

### Race Conditions
**Risk:** Task may be claimed by another user between page load and action

**Mitigation:** Handle API errors gracefully, show error message, refresh task state

### Optimistic Updates
**Risk:** Optimistic UI update may show wrong state if API call fails

**Mitigation:** Revert UI state on error, show error toast
