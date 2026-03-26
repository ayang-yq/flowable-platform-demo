# Task Detail Page Specification

## ADDED Requirements

### Requirement: Display task detail page
The system shall provide a task detail page at `/tasks/[id]` that displays comprehensive task information including metadata, form (if available), and available actions.

#### Scenario: View task details
- **WHEN** a user navigates to `/tasks/[id]` for a task assigned to them
- **THEN** the page displays task name, description, assignee, priority, due date, process instance link, creation time, and available actions

#### Scenario: View unassigned task
- **WHEN** a user navigates to `/tasks/[id]` for an unassigned task
- **THEN** the page displays a "Claim" button and hides the "Complete" button

#### Scenario: Task not found
- **WHEN** a user navigates to `/tasks/[id]` for a non-existent task
- **THEN** the page displays an error message with a link back to the tasks list

### Requirement: Complete task with form
When a form schema exists for the task definition, the system shall render the form and submit variables on completion.

#### Scenario: Complete task with form fields
- **WHEN** a user clicks "Complete" on a task that has a form schema
- **THEN** the system renders the form, validates inputs, and submits `POST /api/tasks/{id}/complete` with the form variables

#### Scenario: Complete task without form
- **WHEN** a user clicks "Complete" on a task with no form schema
- **THEN** the system completes the task directly with no variables and redirects to the instance detail page

#### Scenario: Task completion fails
- **WHEN** task completion API returns an error
- **THEN** the system shows an error message and keeps the user on the task detail page

### Requirement: Navigate to parent instance
The task detail page shall provide a link to the parent process/case instance for context.

#### Scenario: Click instance link
- **WHEN** a user clicks the process/case instance link on the task detail page
- **THEN** the system navigates to `/workspace/[instanceId]?type=[BPMN|CMMN]`

### Requirement: Show task in instance detail
The instance detail page shall display active tasks with inline action buttons.

#### Scenario: View tasks on instance detail
- **WHEN** a user views an active instance detail page with active tasks
- **THEN** each task shows "Claim" (if unassigned) or "Complete" action buttons

#### Scenario: Complete task from instance detail
- **WHEN** a user clicks "Complete" on a task in the instance detail page
- **THEN** the system completes the task and refreshes the instance detail data
