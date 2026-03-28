## MODIFIED Requirements

### Requirement: Highlight active elements
The system SHALL visually highlight which elements in the BPMN diagram are currently active based on the instance execution state.

Completed elements SHALL be shown with green (#22c55e) border styling and a checkmark overlay.

 Active elements SHALL be shown with orange (#f97316) border styling. Current elements SHALL maintain the blue animated pulse overlay.

 A legend SHALL be displayed below the diagram.

 Priority: active > current > completed.

 If an element appears in both active and completed lists, the active styling takes precedence.

#### Scenario: Active user task highlighted
- **WHEN** a user task is currently active in the process instance
- **THEN** the user task element in the diagram is highlighted with orange border styling

 no checkmark overlay

#### Scenario: Completed elements marked
- **WHEN** an activity (task, gateway) has been completed during execution
- **THEN** the completed element is highlighted with green border styling and checkmark badge overlay

#### Scenario: Current element indicator
- **WHEN** the process instance is actively executing
- **THEN** the currently executing element is indicated with animated blue overlay
 AND distinguished from both completed (green) and active (orange) highlights
