## Context

The platform currently supports starting BPMN processes and CMMN cases, but lacks visual representation of running instances. Users can only see instance metadata (start time, status, variables) without understanding the visual flow.

**Current State:**
- Frontend: Next.js 14 with React 18, Tailwind CSS, Lucide React icons
- Backend: Spring Boot 3.5.x, Flowable 7.2.0 (BPMN + CMMN engines)
- Process/case definitions stored in Flowable engines with BPMN/CMMN XML included
- No existing diagram visualization components

**Constraints:**
- Must use industry-standard visualization libraries (bpmn-js, cmmn-js)
- Must support both BPMN and CMMN diagrams
- Must work with multi-tenant architecture
- Cannot modify Flowable engine internals

## Goals / Non-Goals

**Goals:**
- Interactive visualization of running BPMN process instances with active state highlighted
- Interactive visualization of running CMMN case instances with active state highlighted
- Zoom and pan navigation for complex diagrams
- Click-to-complete functionality for user tasks (future enhancement)
- Reusable components that can be used in instance detail views and dashboard

**Non-Goals:**
- Process/case modeler (designer) - out of scope, only viewing running instances
- Real-time websocket updates - initial version loads state on demand
- Historical instance visualization - only active instances
- Custom diagram styling beyond library defaults

## Decisions

### 1. Library Selection
**Decision:** Use bpmn-js (BPMN) and cmmn-js (CMMN) from Camunda

**Rationale:**
- Industry standard, battle-tested libraries
- Active maintenance and documentation
- MIT license (commercial-friendly)
- Lightweight, no heavy dependencies
- Compatible with React

**Alternatives Considered:**
- **bpmn.io properties panel**: More complex, heavier dependency
- **Custom SVG rendering**: Too much maintenance overhead, missing features

### 2. Architecture Pattern
**Decision:** Client-side rendering with backend data fetch

**Rationale:**
- Keep backend stateless and focused on business logic
- Leverage user's browser for rendering performance
- Reduces server load
- Easier to implement caching and optimistic UI updates

**Alternatives Considered:**
- **Server-side rendering (SSR)**: Added complexity, harder to make interactive
- **Pre-rendered images**: Not interactive, harder to style

### 3. State Management
**Decision:** Single API endpoint per instance returning XML + state

**Rationale:**
- Single request reduces latency
- Backend can aggregate data from Flowable engines efficiently
- Simpler frontend logic (no multiple data sources to coordinate)

**API Response Structure:**
```json
{
  "diagramXml": "<?xml version=\"1.0\"...?>",
  "activeElementIds": ["userTask_1", "serviceTask_2"],
  "completedElementIds": ["startEvent_1", "scriptTask_1"],
  "currentElementId": "userTask_1"
}
```

**Alternatives Considered:**
- **Separate endpoints** (/instances/{id}/diagram, /instances/{id}/state): More round trips
- **WebSocket streaming**: Overkill for initial version

### 4. Component Structure
**Decision:** Shared diagram viewer wrapper with BPMN/CMMN specific implementations

**Rationale:**
- Common functionality (zoom, pan, click handling) in shared component
- Engine-specific rendering delegated to bpmn-js/cmmn-js
- Easier to maintain and extend

**Component Hierarchy:**
```
<DiagramViewer baseClassName={engineType}>
  <BpmnViewer /> or <CmmnViewer />
</DiagramViewer>
```

### 5. Styling and Theming
**Decision:** Use default bpmn-js/cmmn-js styling initially

**Rationale:**
- Default styles are professional and recognizable
- Custom theming adds significant complexity
- Can extend later if needed

## Risks / Trade-offs

### Performance
**Risk**: Large diagrams (100+ elements) may render slowly

**Mitigation**:
- Implement lazy loading or canvas rendering for large diagrams
- Add loading states and progressive rendering
- Cache diagram XML in frontend (only fetch state changes)

### Browser Compatibility
**Risk**: bpmn-js/cmmn-js may have compatibility issues with older browsers

**Mitigation**:
- Test in Chrome, Firefox, Safari, Edge
- Document browser requirements
- Fallback to list view if diagram fails to load

### Memory Leaks
**Risk**: Diagram viewers not cleaning up properly on unmount

**Mitigation**:
- Implement cleanup in useEffect return functions
- Destroy bpmn-js/cmmn-js modeler instances explicitly
- Test with React DevTools Profiler

### Data Accuracy
**Risk**: State data may be stale if instance progresses during viewing

**Mitigation**:
- Add timestamp to API response
- Consider refresh button for manual state update
- Document that diagram shows state at load time (not real-time)

## Migration Plan

### Phase 1: Backend API (Day 1-2)
1. Add new API endpoint: `GET /api/instances/{id}/diagram`
2. Fetch diagram XML from Flowable engines
3. Fetch current state from Flowable engines
4. Return aggregated response

### Phase 2: Frontend Libraries (Day 2-3)
1. Install bpmn-js and cmmn-js npm packages
2. Create shared DiagramViewer component
3. Create BpmnViewer and CmmnViewer components
4. Add basic styling and error handling

### Phase 3: Integration (Day 3-4)
1. Integrate diagram viewer into instance detail page
2. Add to dashboard as optional view
3. Test with various BPMN/CMMN processes
4. Document usage

### Rollback Strategy
- Feature behind feature flag initially
- Can revert API changes without breaking existing functionality
- Frontend changes additive (no breaking changes)

## Open Questions

1. **Navigation**: Should clicking elements navigate to task details? (Deferred - future enhancement)
2. **Collaboration**: Should multiple users viewing same instance see live updates? (Deferred - requires WebSockets)
3. **Mobile Support**: How to handle diagrams on mobile screens? (Deferred - responsive design)
4. **Editing**: Will we ever need to edit diagrams in-place? (Out of scope - use Flowable Modeler)
