## Why

Both the BPMN and CMMN diagram viewers already receive `completedElementIds` from the backend API, but the frontend viewers do not visually differentiate completed elements from active ones. Currently all overlays use the same highlighting style, making it impossible to distinguish which steps have already been executed versus which are still running. This reduces the usefulness of the diagram as a process monitoring tool.

## What Changes

- Add distinct visual styling (e.g., green border with checkmark overlay) for completed elements in the BPMN diagram viewer
- Add distinct visual styling for completed elements in the CMMN diagram viewer
- Ensure completed, active, and current element highlights are visually distinct and consistent across both viewers
- Add a legend explaining the highlight color meanings

## Capabilities

### New Capabilities
- `diagram-completed-highlight`: Visual differentiation of completed diagram elements with distinct overlays and legend

### Modified Capabilities
- `bpmn-instance-viewer`: Adding completed element highlight styling alongside existing active/current highlights
- `cmmn-instance-viewer`: Adding completed element highlight styling alongside existing active/current highlights

## Impact

- Frontend only: `BpmnViewer` and `CmmnViewer` components need overlay style updates
- No backend changes required — `completedElementIds` is already provided by the API
- No breaking changes to existing APIs or data structures
