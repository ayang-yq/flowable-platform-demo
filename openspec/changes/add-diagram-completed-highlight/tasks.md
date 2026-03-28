## 1. Shared Highlight Styles

- [x] 1.1 Add CSS classes for completed highlight (green border + checkmark badge) to the global diagram styles or a file `frontend/src/components/diagram/diagram-overlays.css`
- [x] 1.2 Create shared `DiagramLegend` component in `frontend/src/components/diagram/DiagramLegend.tsx`

## 2. BPMN Viewer Updates

- [x] 2.1 Update `BpmnViewer` to apply completed element overlays using `completedElementIds` prop
- [x] 2.2 Add `DiagramLegend` below the BPMN diagram canvas
- [x] 2.3 Ensure active/current/completed priority ordering is overlay logic

## 3. CMMN Viewer Updates

- [x] 3.1 Update `CmmnViewer` to apply completed element overlays using `completedElementIds` prop
- [x] 3.2 Add `DiagramLegend` below the CMMN diagram canvas
- [x] 3.3 Ensure active/current/completed priority ordering in overlay logic

## 4. Testing

- [x] 4.1 Verify completed elements display green styling with checkmark in BPMN viewer
- [x] 4.2 Verify completed elements display green styling with checkmark in CMMN viewer
- [x] 4.3 Verify legend renders correctly below both viewers
- [x] 4.4 Verify active takes visual precedence over completed for overlapping element IDs
