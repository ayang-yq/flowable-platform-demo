# Feature Specification: Workspace Instance Management Dashboard

**Feature Branch**: `004-workspace-instance-dashboard`
**Created**: 2026-03-23
**Status**: Draft
**Input**: User description: "Refer to Workspace in Flowable Enterprise Edition, create a new feature to let user can create instance for CMMN, BPMN and DMN models. And a dashboard to list all active and completed instances."

## Clarifications

### Session 2026-03-23

- Q: Should the dashboard show only the current user's instances or all tenant instances? → A: All tenant instances, with a "My Instances" filter toggle to let users focus on their own work.
- Q: How should the dashboard update when instance states change? → A: Polling every 10-15 seconds (no WebSocket/SSE required).
- Q: Where should cancelled/failed instances appear? → A: In the Completed tab with a distinct status badge (Completed, Cancelled, Failed).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Start a BPMN Process Instance (Priority: P1)

As a workspace user, I want to start a new process instance from a deployed BPMN model so that I can initiate business processes directly from the platform workspace.

The user navigates to a "Start Instance" area in the workspace, sees a list of available BPMN process definitions, selects one, optionally fills in any required start form fields, and starts the process. The new instance appears in the active instances dashboard.

**Why this priority**: Starting BPMN process instances is the most common workflow action and forms the core of the workspace experience, similar to Flowable Enterprise's Workspace app.

**Independent Test**: Can be fully tested by deploying a sample BPMN model, starting an instance from the UI, and verifying it appears in the active instances list.

**Acceptance Scenarios**:

1. **Given** a user is logged in and BPMN models are deployed, **When** the user navigates to the start instance area, **Then** the system displays a list of available BPMN process definitions grouped by category.
2. **Given** a user selects a BPMN process definition with a start form, **When** the user fills in required fields and clicks "Start", **Then** a new process instance is created and the user is redirected to the active instances dashboard showing the new instance.
3. **Given** a user selects a BPMN process definition without a start form, **When** the user clicks "Start", **Then** a new process instance is created immediately and a success confirmation is shown.
4. **Given** a user attempts to start a process instance with invalid form data, **When** the user submits the form, **Then** validation errors are displayed and the instance is not created.

---

### User Story 2 - Start a CMMN Case Instance (Priority: P1)

As a workspace user, I want to start a new case instance from a deployed CMMN model so that I can initiate case management workflows from the platform workspace.

The user selects from available CMMN case definitions, optionally fills in start form fields, and creates a new case instance.

**Why this priority**: CMMN case management is equally important as BPMN for enterprise users who need flexible, event-driven case handling.

**Independent Test**: Can be fully tested by deploying a sample CMMN model, starting a case instance, and verifying it appears in the active instances list.

**Acceptance Scenarios**:

1. **Given** a user is logged in and CMMN models are deployed, **When** the user navigates to the start instance area, **Then** the system displays available CMMN case definitions alongside BPMN definitions, clearly distinguished by type.
2. **Given** a user selects a CMMN case definition and fills in required start form fields, **When** the user clicks "Start", **Then** a new case instance is created and appears in the active instances dashboard.
3. **Given** no CMMN models are deployed, **When** the user views the start instance area, **Then** a helpful message indicates no CMMN case definitions are available.

---

### User Story 3 - Active Instances Dashboard (Priority: P1)

As a workspace user, I want to view a dashboard listing all active (in-progress) process and case instances across my tenant so that I can monitor ongoing work and take action on items.

The dashboard shows all tenant instances by default — a unified list of active BPMN process instances and CMMN case instances with key details such as instance name, type (BPMN/CMMN), start time, current state, and the user who started it. A "My Instances" toggle allows users to filter to only instances they started.

**Why this priority**: Visibility into active work is essential for users to manage and monitor their workflows, making this a core dashboard feature.

**Independent Test**: Can be fully tested by starting several instances and verifying they all appear in the active instances dashboard with correct details and filtering.

**Acceptance Scenarios**:

1. **Given** a user is logged in and active instances exist, **When** the user navigates to the instances dashboard, **Then** the system displays a list of all active BPMN and CMMN instances in the tenant with name, type, start time, started by, and current state.
2. **Given** many active instances exist, **When** the user views the dashboard, **Then** the list is paginated and the user can navigate between pages.
3. **Given** the user wants to find a specific instance, **When** the user uses the search/filter controls, **Then** the list is filtered by instance name, type (BPMN/CMMN), or date range.
4. **Given** the user clicks on an active instance, **When** the detail view opens, **Then** the system displays instance details including variables, current activities/tasks, and a visual progress indicator.
5. **Given** the user wants to see only their own instances, **When** the user enables the "My Instances" toggle, **Then** the dashboard filters to show only instances started by the current user.

---

### User Story 4 - Completed Instances Dashboard (Priority: P2)

As a workspace user, I want to view a list of completed process and case instances so that I can review historical work and audit past executions.

The dashboard provides a view of completed instances with end time, duration, and outcome information. Like the active dashboard, it shows all tenant instances by default with a "My Instances" toggle.

**Why this priority**: Historical visibility is important for audit and review but is secondary to managing active work.

**Independent Test**: Can be fully tested by completing instances and verifying they move from active to completed view with correct end-time and duration data.

**Acceptance Scenarios**:

1. **Given** completed, cancelled, or failed instances exist, **When** the user switches to the "Completed" tab on the dashboard, **Then** the system displays all finished BPMN and CMMN instances with name, type, start time, end time, duration, and a distinct status badge (Completed, Cancelled, or Failed).
2. **Given** the user wants to review a completed instance, **When** the user clicks on a completed instance, **Then** the system shows instance details including final variable values and completion outcome.
3. **Given** many completed instances exist, **When** the user views the completed tab, **Then** the list supports pagination, search, and filtering by type, date range, and name.

---

### User Story 5 - Execute a DMN Decision (Priority: P2)

As a workspace user, I want to evaluate a deployed DMN decision model by providing input values and viewing the decision output, so that I can test and execute decision logic from the workspace.

The user selects a DMN decision definition, provides input values through a form, and the system evaluates the decision table and returns the output results.

**Why this priority**: DMN decision evaluation completes the trifecta of BPMN/CMMN/DMN support but is used less frequently as a standalone action compared to process and case instances.

**Independent Test**: Can be fully tested by deploying a DMN model, submitting input values, and verifying the correct decision output is returned and displayed.

**Acceptance Scenarios**:

1. **Given** DMN models are deployed, **When** the user navigates to the start instance area, **Then** DMN decision definitions are listed alongside BPMN and CMMN definitions, clearly distinguished by type.
2. **Given** a user selects a DMN decision definition, **When** the user provides input values and clicks "Execute", **Then** the system evaluates the decision and displays the output results.
3. **Given** a user provides incomplete or invalid input for a DMN decision, **When** the user submits, **Then** the system displays appropriate validation messages.
4. **Given** a DMN decision has been executed, **When** the user views historical executions, **Then** the execution appears in the completed instances view with input/output details.

---

### User Story 6 - Dashboard Summary Statistics (Priority: P3)

As a workspace user, I want to see summary statistics at the top of the dashboard (total active, total completed, started today, etc.) so that I can quickly understand workload at a glance.

**Why this priority**: Summary statistics enhance the dashboard experience but are not essential for core functionality.

**Independent Test**: Can be fully tested by verifying the counts match the actual number of instances in the system.

**Acceptance Scenarios**:

1. **Given** instances exist in the system, **When** the user views the dashboard, **Then** summary cards display counts for total active instances, total completed instances, and instances started today.
2. **Given** no instances exist, **When** the user views the dashboard, **Then** summary cards show zero counts with a helpful "Get Started" message.

---

### Edge Cases

- What happens when a user tries to start an instance for a model that has been undeployed between page load and submission? The system should show an error message that the definition is no longer available and refresh the list.
- How does the system handle a process/case instance that fails during creation? The system should display a clear error message with details and not leave orphaned data.
- What happens when the user has no permission to start a specific model? The model should not appear in the available definitions list, or if permissions change mid-session, the start action should return an authorization error.
- How does the dashboard handle instances started by other users in the same tenant? All tenant instances are visible by default; the "My Instances" toggle lets users filter to their own instances.
- What happens with very long-running instances (months/years)? Duration display should use appropriate units (days, months) and the instance should remain in the active view.
- How are cancelled and failed instances handled? They appear in the Completed tab with a distinct status badge (Completed, Cancelled, Failed) so users can distinguish outcomes at a glance.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display a unified list of available BPMN process definitions, CMMN case definitions, and DMN decision definitions that the current user can start/execute.
- **FR-002**: System MUST allow users to start a new BPMN process instance from any available process definition, with optional start form support.
- **FR-003**: System MUST allow users to start a new CMMN case instance from any available case definition, with optional start form support.
- **FR-004**: System MUST allow users to execute a DMN decision by providing input values and displaying output results.
- **FR-005**: System MUST provide an active instances dashboard showing all in-progress BPMN process instances and CMMN case instances across the tenant.
- **FR-006**: System MUST provide a completed instances dashboard showing all finished, cancelled, and failed BPMN process instances and CMMN case instances, including DMN execution history, with distinct status badges (Completed, Cancelled, Failed).
- **FR-007**: System MUST support pagination for instance lists with a configurable page size.
- **FR-008**: System MUST support searching and filtering instances by name, type (BPMN/CMMN/DMN), date range, and status.
- **FR-009**: System MUST display instance details including variables, current state, timestamps, and the initiating user.
- **FR-010**: System MUST display summary statistics (active count, completed count, started today) on the dashboard.
- **FR-011**: System MUST support start form input when starting instances. MVP: simple key-value variable input form. Future enhancement: integrate existing FormService/SurveyJS for dynamic form rendering based on the model's form definition.
- **FR-012**: System MUST validate form inputs before creating an instance and display clear error messages for invalid data.
- **FR-013**: System MUST visually distinguish between BPMN, CMMN, and DMN items using icons and/or labels throughout the interface.
- **FR-014**: System MUST automatically refresh dashboard data via polling every 10-15 seconds to reflect instance state changes.
- **FR-015**: System MUST provide a "My Instances" toggle on both active and completed dashboards to filter instances to only those started by the current user.

### Workflow Requirements

- **WFR-001**: Process/case instance creation MUST use the appropriate engine runtime services to start instances with the correct tenant context.
- **WFR-002**: Instance queries MUST use history services for completed instances and runtime services for active instances.
- **WFR-003**: DMN decision execution MUST use the decision engine to evaluate decision tables.
- **WFR-004**: Start form availability MUST be detected from the process/case definition metadata (hasStartFormKey). MVP: the presence of a form key triggers a generic variable input form. Future enhancement: retrieve and render the actual form schema from the custom FormService.
- **WFR-005**: All instance operations MUST respect the user's roles and permissions within the tenant.

### Multi-Tenant Requirements

- **TFR-001**: All instance queries MUST filter by the current user's tenant context.
- **TFR-002**: Available definitions MUST only include models deployed to the current tenant.
- **TFR-003**: Cross-tenant instance access MUST be prohibited at both the service and UI levels.

### Key Entities

- **Process Definition**: A deployed BPMN model available for starting instances. Key attributes: name, key, version, category, deployment time, has start form.
- **Case Definition**: A deployed CMMN model available for starting case instances. Key attributes: name, key, version, category, deployment time, has start form.
- **Decision Definition**: A deployed DMN model available for execution. Key attributes: name, key, version, category, decision type.
- **Process Instance**: A running or completed BPMN process execution. Key attributes: name, definition reference, start time, end time, state, started by user, variables.
- **Case Instance**: A running or completed CMMN case execution. Key attributes: name, definition reference, start time, end time, state, started by user, variables.
- **Decision Execution**: A record of a DMN decision evaluation. Key attributes: definition reference, execution time, input values, output values, executed by user.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can start a BPMN process instance, CMMN case instance, or execute a DMN decision within 30 seconds of navigating to the workspace.
- **SC-002**: The active instances dashboard loads and displays results within 2 seconds for up to 1,000 active instances.
- **SC-003**: Users can find a specific instance using search/filter within 10 seconds.
- **SC-004**: 90% of users can start their first instance without requiring help or documentation.
- **SC-005**: The dashboard accurately reflects instance state changes within 15 seconds of the change occurring (polling interval).
- **SC-006**: All three model types (BPMN, CMMN, DMN) are clearly distinguishable at a glance on every screen where they appear.

## Assumptions

- Users have already been authenticated and have an active tenant context (handled by existing platform login from feature 001/003).
- BPMN, CMMN, and DMN models have already been deployed to the engines (model deployment/design is out of scope for this feature).
- The existing navigation infrastructure from feature 003 will be extended with new workspace menu items.
- Start forms use the built-in form engine; custom form renderers are out of scope.
- The dashboard shows instances for the current tenant only; cross-tenant dashboards are out of scope.
- Instance detail views show read-only information; task completion/claim actions are out of scope for this feature (future enhancement).
