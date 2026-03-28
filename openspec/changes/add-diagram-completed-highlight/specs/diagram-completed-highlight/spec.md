## ADDED Requirements

### Requirement: Completed element visual highlight
The system SHALL render completed diagram elements with a distinct green visual style that differentiates them from active and current elements.

#### Scenario: Completed BPMN element styling
- **WHEN** a BPMN element ID appears in `completedElementIds`
- **THEN** the element renders with a green (#22c55e) border/outline overlay
- **AND** a small checkmark badge icon is displayed on the element

#### Scenario: Completed CMMN element styling
- **WHEN** a CMMN plan item ID appears in `completedElementIds`
- **THEN** the plan item renders with a green (#22c55e) border/outline overlay
- **AND** a small checkmark badge icon is displayed on the element

#### Scenario: Element with multiple states
- **WHEN** an element ID appears in both `completedElementIds` and `activeElementIds`
- **THEN** the active highlight takes visual precedence over completed

### Requirement: Visual distinction between states
The system SHALL render three visually distinct highlight states for diagram elements.

#### Scenario: Completed vs active distinction
- **WHEN** viewing a diagram with both completed and active elements
- **THEN** completed elements display green (#22c55e) styling with checkmark
- **AND** active elements display orange (#f97316) styling without checkmark
- **AND** the two states are clearly distinguishable at a glance

#### Scenario: Current element pulse animation
- **WHEN** an element is identified as the `currentElementId`
- **THEN** the element displays a blue animated pulse overlay
- **AND** the pulse animation is distinct from both completed and active styling

### Requirement: Diagram legend
The system SHALL display a legend below each diagram viewer explaining the highlight color meanings.

#### Scenario: Legend display
- **WHEN** a BPMN or CMMN diagram is rendered
- **THEN** a legend bar appears below the diagram canvas
- **AND** the legend shows three colored indicators: green = "Completed", orange = "Active", blue = "Current"

#### Scenario: Legend with no highlights
- **WHEN** a diagram renders with no completed, active, or current elements
- **THEN** the legend is still displayed for reference
