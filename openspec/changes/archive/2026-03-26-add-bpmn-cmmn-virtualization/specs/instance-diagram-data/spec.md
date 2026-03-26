# Instance Diagram Data API Specification

## ADDED Requirements

### Requirement: Provide BPMN instance diagram data
The system SHALL provide an API endpoint that returns the BPMN diagram XML and current state for a specific BPMN process instance.

#### Scenario: Fetch BPMN diagram successfully
- **GIVEN** an active BPMN process instance exists with ID "process-123"
- **WHEN** user sends GET request to `/api/process-instances/process-123/diagram`
- **THEN** system responds with HTTP 200
- **AND** response body includes:
  - `diagramXml`: String containing the BPMN 2.0 XML
  - `activeElementIds`: Array of element IDs currently active
  - `completedElementIds`: Array of element IDs that have completed
  - `currentElementId`: String ID of the currently executing element (or null)

#### Scenario: BPMN instance not found
- **GIVEN** no BPMN process instance exists with ID "nonexistent"
- **WHEN** user sends GET request to `/api/process-instances/nonexistent/diagram`
- **THEN** system responds with HTTP 404
- **AND** response includes error message: "Process instance not found"

### Requirement: Provide CMMN instance diagram data
The system SHALL provide an API endpoint that returns the CMMN diagram XML and current state for a specific CMMN case instance.

#### Scenario: Fetch CMMN diagram successfully
- **GIVEN** an active CMMN case instance exists with ID "case-456"
- **WHEN** user sends GET request to `/api/case-instances/case-456/diagram`
- **THEN** system responds with HTTP 200
- **AND** response body includes:
  - `diagramXml`: String containing the CMMN 1.1 XML
  - `activeElementIds`: Array of plan item IDs currently active
  - `completedElementIds`: Array of plan item IDs that have completed
  - `currentElementId`: String ID of the currently executing element (or null)

#### Scenario: CMMN instance not found
- **GIVEN** no CMMN case instance exists with ID "nonexistent"
- **WHEN** user sends GET request to `/api/case-instances/nonexistent/diagram`
- **THEN** system responds with HTTP 404
- **AND** response includes error message: "Case instance not found"

### Requirement: Retrieve diagram from process definition
The system SHALL fetch the BPMN diagram XML from the process definition associated with the instance.

#### Scenario: Diagram XML available
- **GIVEN** the process instance's definition has associated BPMN XML
- **WHEN** system retrieves diagram data
- **THEN** `diagramXml` field contains the complete BPMN 2.0 XML
- **AND** XML is valid per BPMN 2.0 specification
- **AND** XML includes all diagram elements (tasks, gateways, events, flows)

#### Scenario: No diagram XML available
- **GIVEN** the process instance's definition has no BPMN XML resource
- **WHEN** system attempts to retrieve diagram data
- **THEN** `diagramXml` field is null or empty string
- **AND** system returns HTTP 200 with warning indication

### Requirement: Retrieve diagram from case definition
The system SHALL fetch the CMMN diagram XML from the case definition associated with the instance.

#### Scenario: Diagram XML available
- **GIVEN** the case instance's definition has associated CMMN XML
- **WHEN** system retrieves diagram data
- **THEN** `diagramXml` field contains the complete CMMN 1.1 XML
- **AND** XML is valid per CMMN 1.1 specification
- **AND** XML includes all diagram elements (stages, tasks, milestones, events)

#### Scenario: No diagram XML available
- **GIVEN** the case instance's definition has no CMMN XML resource
- **WHEN** system attempts to retrieve diagram data
- **THEN** `diagramXml` field is null or empty string
- **AND** system returns HTTP 200 with warning indication

### Requirement: Calculate active elements
The system SHALL determine which BPMN elements are currently active based on the instance execution state.

#### Scenario: Active user task
- **GIVEN** a user task is currently assigned and waiting for completion
- **THEN** `activeElementIds` includes the user task's element ID
- **AND** `currentElementId` is set to the user task's element ID

#### Scenario: Active service task
- **GIVEN** a service task is currently executing asynchronously
- **THEN** `activeElementIds` includes the service task's element ID
- **AND** `currentElementId` may be set to the service task's element ID

#### Scenario: Process completed
- **GIVEN** the process instance has reached its end event
- **THEN** `activeElementIds` is an empty array
- **AND** `currentElementId` is null

### Requirement: Calculate completed elements
The system SHALL determine which BPMN elements have been completed during instance execution.

#### Scenario: Completed sequence flow
- **GIVEN** the process has completed a sequence flow from start event to user task
- **THEN** `completedElementIds` includes IDs of all traversed elements
- **AND** includes the start event ID, any gateways, and intermediate tasks

#### Scenario: Parallel gateway completion
- **GIVEN** the process has completed a parallel gateway branch
- **THEN** `completedElementIds` includes the parallel gateway ID
- **AND** includes all element IDs from all completed branches

#### Scenario: Loop activity completion
- **GIVEN** a task within a loop has completed N iterations
- **THEN** `completedElementIds` includes the task's element ID once
- **AND** does not duplicate the element ID for each iteration

### Requirement: Calculate CMMN active elements
The system SHALL determine which CMMN plan items are currently active based on the case instance execution state.

#### Scenario: Active human task
- **GIVEN** a human task is currently active in the case
- **THEN** `activeElementIds` includes the human task's plan item ID
- **AND** `currentElementId` is set to the human task's plan item ID

#### Scenario: Active stage
- **GIVEN** a case stage is currently active (not yet completed)
- **THEN** `activeElementIds` includes the stage's plan item ID
- **AND** `activeElementIds` may include IDs of active child tasks within the stage

#### Scenario: Case completed
- **GIVEN** the case instance has reached its termination
- **THEN** `activeElementIds` is an empty array
- **AND** `currentElementId` is null

### Requirement: Calculate CMMN completed elements
The system SHALL determine which CMMN plan items have been completed during instance execution.

#### Scenario: Completed stage
- **GIVEN** a case stage has completed and exited
- **THEN** `completedElementIds` includes the stage's plan item ID
- **AND** `completedElementIds` includes all child element IDs that completed within the stage

#### Scenario: Completed milestone
- **GIVEN** a milestone has been achieved during case execution
- **THEN** `completedElementIds` includes the milestone's plan item ID

#### Scenario: Completed task
- **GIVEN** a human task or service task has completed
- **THEN** `completedElementIds` includes the task's plan item ID

### Requirement: Support tenant isolation
The system SHALL ensure users can only access diagram data for instances they have permission to view.

#### Scenario: Tenant-specific instance access
- **GIVEN** user is authenticated with tenant ID "tenant-abc"
- **WHEN** user requests diagram for instance with tenant ID "tenant-abc"
- **THEN** system returns diagram data

#### Scenario: Cross-tenant access denied
- **GIVEN** user is authenticated with tenant ID "tenant-abc"
- **WHEN** user requests diagram for instance with different tenant ID "tenant-xyz"
- **THEN** system responds with HTTP 403 or 404
- **AND** response includes error message about access denied

#### Scenario: Global instance access
- **GIVEN** user is authenticated in any tenant
- **WHEN** user requests diagram for global instance (tenant ID is empty string)
- **THEN** system returns diagram data

### Requirement: Authentication required
The system SHALL require valid authentication to access diagram data endpoints.

#### Scenario: Unauthenticated access denied
- **WHEN** unauthenticated user sends GET request to diagram endpoint
- **THEN** system responds with HTTP 401
- **AND** response includes error message: "Authentication required"

### Requirement: Response format
The system SHALL return diagram data in a consistent JSON format.

#### Scenario: BPMN diagram response structure
- **WHEN** system successfully returns BPMN diagram data
- **THEN** response Content-Type is "application/json"
- **AND** response body structure is:
  ```json
  {
    "diagramXml": "<?xml...?>",
    "activeElementIds": ["userTask_1", "serviceTask_2"],
    "completedElementIds": ["startEvent_1", "scriptTask_1"],
    "currentElementId": "userTask_1"
  }
  ```

#### Scenario: CMMN diagram response structure
- **WHEN** system successfully returns CMMN diagram data
- **THEN** response Content-Type is "application/json"
- **AND** response body structure is:
  ```json
  {
    "diagramXml": "<?xml...?>",
    "activeElementIds": ["humanTask_1", "stage_1"],
    "completedElementIds": ["milestone_1", "task_1"],
    "currentElementId": "humanTask_1"
  }
  ```

### Requirement: Performance requirements
The system SHALL return diagram data responses within acceptable time limits.

#### Scenario: Response time for typical instance
- **GIVEN** a process or case instance with typical complexity (≤50 elements)
- **WHEN** authenticated user requests diagram data
- **THEN** system responds within 1 second

#### Scenario: Response time for complex instance
- **GIVEN** a process or case instance with high complexity (100-200 elements)
- **WHEN** authenticated user requests diagram data
- **THEN** system responds within 3 seconds
