## Why

The platform has task pages and backend APIs for claiming/completing tasks, but the frontend task cards display action buttons that are not connected to any API calls. There is no task detail page (`/tasks/[id]`), no task completion form, and the API client lacks task-specific methods. Users cannot actually interact with tasks — they can only view lists.

## What Changes

- Add a task detail page at `/tasks/[id]` showing task metadata, form (if available), variables, and actions
- Connect task action buttons (Claim, Complete, Delegate) in TaskCard to backend API calls
- Add task API methods to the frontend API client
- Show active tasks on the instance detail page with inline claim/complete actions
- Add task form rendering for task completion with variable input

## Capabilities

### New Capabilities
- `task-detail-page`: Task detail page with metadata, form rendering, and action handling
- `task-actions`: API client methods and frontend integration for claim/complete/delegate operations

### Modified Capabilities
- `instance-diagram-data`: Instance detail page enhanced with task actions (claim/complete) inline

## Impact

- **Frontend**: New `/tasks/[id]` page, updated `TaskCard` component, updated `api.ts`, updated instance detail page
- **Backend**: No changes needed — all API endpoints already exist in `TaskController`
- **Dependencies**: Uses existing form schema system (`form_schemas` table) for task completion forms
