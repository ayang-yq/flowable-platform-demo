# Task Actions Specification

## ADDED Requirements

### Requirement: Claim a task
Users shall be able to claim unassigned tasks from the task list and detail pages.

#### Scenario: Claim task from task card
- **WHEN** a user clicks "Claim" on an unassigned task card
- **THEN** the system calls `POST /api/tasks/{id}/claim`, optimistically updates the UI, and refreshes the task list on success

#### Scenario: Claim already assigned task
- **WHEN** a user tries to claim a task that is already assigned
- **THEN** the API returns an error and the system shows an error message

### Requirement: Complete a task
Users shall be able to complete tasks they are assigned to.

#### Scenario: Complete assigned task
- **WHEN** a user clicks "Complete" on a task assigned to them
- **THEN** the system calls `POST /api/tasks/{id}/complete` and redirects to the instance detail page

#### Scenario: Complete unassigned task
- **WHEN** a user tries to complete a task not assigned to them
- **THEN** the system shows an error message

### Requirement: Delegate a task
Users shall be able to delegate tasks to other users.

#### Scenario: Delegate task
- **WHEN** a user clicks "Delegate" on a task and selects a target user
- **THEN** the system calls `POST /api/tasks/{id}/delegate` with the target username and refreshes the task list

### Requirement: API client task methods
The frontend API client shall expose methods for all task operations.

#### Scenario: Task API client available
- **WHEN** the frontend needs to perform task operations
- **THEN** the `apiClient` object provides `getTask(id)`, `claimTask(id)`, `completeTask(id, variables)`, `delegateTask(id, username)` methods
