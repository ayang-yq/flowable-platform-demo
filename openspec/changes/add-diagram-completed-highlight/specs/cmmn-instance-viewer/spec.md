## MODIFIED Requirements

### Requirement: Highlight active elements
The system SHALL visually highlight which elements in the CMMN diagram are currently active based on the case instance execution state.

 Completed elements SHALL be shown with green (#22c55e) border styling and a checkmark badge overlay.
 Active elements SHALL maintain orange (#f97316) border styling.
 Current elements SHALL maintain animated distinct visual marker.
 A legend SHALL be displayed below the diagram.
 Priority: active > current > completed.
 If an element appears in both active and completed lists, the active styling takes precedence.

#### Scenario: Active human task highlighted
- **WHEN** a human task is currently active in the case instance
- **THEN** the human task element in the diagram is highlighted with orange border styling
 no checkmark overlay

#### Scenario: Completed elements marked
- **WHEN** a plan item (task, stage, milestone) has been completed during execution
- **THEN** the completed element is visually marked with green border styling and checkmark badge overlay

#### Scenario: Completed stage indicator
- **WHEN** a case stage has completed all required plan items
- **THEN** the stage element is visually marked as completed with green styling
- **AND** the stage name or border indicates completion status

#### Scenario: Current element indicator
- **WHEN** the case instance is actively executing
- **THEN** the currently executing element is indicated with animated blue overlay
- **AND** distinguished from both completed (green) and active (orange) highlights

