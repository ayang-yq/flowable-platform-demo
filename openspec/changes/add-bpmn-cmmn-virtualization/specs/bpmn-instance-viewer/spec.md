# BPMN Instance Viewer Specification

## ADDED Requirements

### Requirement: Display BPMN instance diagram
The system SHALL display a visual BPMN 2.0 diagram for any running BPMN process instance, rendered using the bpmn-js library.

#### Scenario: View diagram for active instance
- **WHEN** user navigates to an active BPMN process instance detail page
- **THEN** system displays the BPMN diagram for that process instance
- **AND** the diagram is rendered using bpmn-js library
- **AND** the diagram includes all process elements (events, tasks, gateways, flows)

### Requirement: Highlight active elements
The system SHALL visually highlight which elements in the BPMN diagram are currently active based on the instance execution state.

#### Scenario: Active user task highlighted
- **WHEN** a user task is currently active in the process instance
- **THEN** the user task element in the diagram is highlighted with a distinct visual style (e.g., orange border or background)

#### Scenario: Completed elements marked
- **WHEN** an activity (task, gateway) has been completed during execution
- **THEN** the completed element is visually marked (e.g., grayed out or checkmark overlay)

#### Scenario: Current element indicator
- **WHEN** the process instance is actively executing
- **THEN** the currently executing element is indicated with an animated or distinct visual marker

### Requirement: Provide diagram navigation
The system SHALL provide zoom and pan controls for navigating large BPMN diagrams.

#### Scenario: Zoom in/out
- **WHEN** user clicks zoom in button
- **THEN** diagram magnification increases by 25%
- **WHEN** user clicks zoom out button
- **THEN** diagram magnification decreases by 25%

#### Scenario: Pan diagram
- **WHEN** user clicks and drags on the diagram canvas
- **THEN** the diagram pans in the direction of the drag
- **AND** the user can reach any part of the diagram

#### Scenario: Fit to screen
- **WHEN** user clicks "fit to screen" button
- **THEN** the entire diagram is scaled to fit within the visible container

### Requirement: Display element information
The system SHALL display information about diagram elements when user interacts with them.

#### Scenario: View element details
- **WHEN** user hovers over a BPMN element (task, gateway, event)
- **THEN** system displays a tooltip with element details (name, type, ID)

#### Scenario: Click to view task details
- **WHEN** user clicks on a user task element in the diagram
- **THEN** system navigates to detailed view of that task (if implemented)
- **OR** system shows task details in a side panel (if implemented)

### Requirement: Handle loading and error states
The system SHALL gracefully handle scenarios where the diagram cannot be loaded.

#### Scenario: Diagram loading
- **WHEN** the diagram data is being fetched from the backend
- **THEN** system displays a loading indicator (spinner or skeleton)
- **AND** user cannot interact with the diagram until loaded

#### Scenario: Diagram load failure
- **WHEN** the diagram XML fails to load or parse
- **THEN** system displays an error message: "Unable to load diagram"
- **AND** system offers a retry button

#### Scenario: No diagram available
- **WHEN** the process definition has no associated diagram XML
- **THEN** system displays message: "No diagram available for this process"
- **AND** system shows instance metadata instead

### Requirement: Responsive display
The system SHALL display BPMN diagrams appropriately on different screen sizes.

#### Scenario: Desktop view
- **WHEN** diagram is viewed on a desktop screen (≥1024px width)
- **THEN** the diagram viewer takes up 80-100% of the available content width
- **AND** the diagram height is at least 400px

#### Scenario: Mobile view
- **WHEN** diagram is viewed on a mobile screen (<768px width)
- **THEN** the diagram viewer uses full available width
- **AND** zoom controls are consolidated or moved to a menu

### Requirement: Performance requirements
The system SHALL load and render BPMN diagrams efficiently.

#### Scenario: Load time
- **WHEN** user opens a BPMN instance with a typical diagram (≤50 elements)
- **THEN** the diagram is fully rendered within 3 seconds

#### Scenario: Large diagram rendering
- **WHEN** user opens a BPMN instance with a large diagram (100+ elements)
- **THEN** the diagram renders within 10 seconds
- **AND** system shows loading progress during rendering
