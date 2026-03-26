# CMMN Instance Viewer Specification

## ADDED Requirements

### Requirement: Display CMMN instance diagram
The system SHALL display a visual CMMN 1.1 diagram for any running CMMN case instance, rendered using the cmmn-js library.

#### Scenario: View diagram for active case
- **WHEN** user navigates to an active CMMN case instance detail page
- **THEN** system displays the CMMN diagram for that case instance
- **AND** the diagram is rendered using cmmn-js library
- **AND** the diagram includes all case elements (stages, tasks, milestones, events)

### Requirement: Highlight active elements
The system SHALL visually highlight which elements in the CMMN diagram are currently active based on the case instance execution state.

#### Scenario: Active human task highlighted
- **WHEN** a human task is currently active in the case instance
- **THEN** the human task element in the diagram is highlighted with a distinct visual style (e.g., orange border or background)

#### Scenario: Active stage highlighted
- **WHEN** a case stage is currently active
- **THEN** the entire stage element in the diagram is highlighted
- **AND** all contained elements are visible

#### Scenario: Completed elements marked
- **WHEN** a plan item (task, stage, milestone) has been completed during execution
- **THEN** the completed element is visually marked (e.g., grayed out or checkmark overlay)

#### Scenario: Completed stage indicator
- **WHEN** a case stage has completed all its required plan items
- **THEN** the stage element is visually marked as completed
- **AND** the stage name or border indicates completion status

#### Scenario: Current element indicator
- **WHEN** the case instance is actively executing
- **THEN** the currently executing element is indicated with an animated or distinct visual marker

### Requirement: Provide diagram navigation
The system SHALL provide zoom and pan controls for navigating large CMMN diagrams.

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
- **WHEN** user hovers over a CMMN element (task, stage, milestone, event)
- **THEN** system displays a tooltip with element details (name, type, ID)

#### Scenario: Click to view task details
- **WHEN** user clicks on a human task element in the diagram
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
- **WHEN** the case definition has no associated diagram XML
- **THEN** system displays message: "No diagram available for this case"
- **AND** system shows instance metadata instead

### Requirement: Responsive display
The system SHALL display CMMN diagrams appropriately on different screen sizes.

#### Scenario: Desktop view
- **WHEN** diagram is viewed on a desktop screen (≥1024px width)
- **THEN** the diagram viewer takes up 80-100% of the available content width
- **AND** the diagram height is at least 400px

#### Scenario: Mobile view
- **WHEN** diagram is viewed on a mobile screen (<768px width)
- **THEN** the diagram viewer uses full available width
- **AND** zoom controls are consolidated or moved to a menu

### Requirement: Performance requirements
The system SHALL load and render CMMN diagrams efficiently.

#### Scenario: Load time
- **WHEN** user opens a CMMN instance with a typical diagram (≤50 elements)
- **THEN** the diagram is fully rendered within 3 seconds

#### Scenario: Large diagram rendering
- **WHEN** user opens a CMMN instance with a large diagram (100+ elements)
- **THEN** the diagram renders within 10 seconds
- **AND** system shows loading progress during rendering

### Requirement: Stage expansion support
The system SHALL support displaying collapsed and expanded states for case stages.

#### Scenario: Collapsed stage view
- **WHEN** a case stage is in collapsed state
- **THEN** the stage element shows minimal information (name, status)
- **AND** contained plan items are not visible in the diagram

#### Scenario: Expanded stage view
- **WHEN** a case stage is in expanded state
- **THEN** the stage element shows all contained plan items
- **AND** all child elements are visible and interactive

### Requirement: Milestone visualization
The system SHALL appropriately display case milestones in the diagram.

#### Scenario: Milestone achieved
- **WHEN** a milestone has been reached during case execution
- **THEN** the milestone element is visually marked as achieved
- **AND** the visual indicator is distinct from plan item completion

#### Scenario: Milestone pending
- **WHEN** a milestone has not yet been reached
- **THEN** the milestone element is shown in its pending state (not highlighted)
