## Why

Currently, the platform can start BPMN and CMMN instances but users cannot visualize the running process/case state. Users need to see the visual diagram of their running instances with current activity highlighted, understand which tasks/stages are active, and interact with the instance through the diagram.

BPMN.js and CMMN.js are industry-standard JavaScript libraries that provide interactive visualization of process and case models. Adding this will significantly improve user experience and operational visibility.

## What Changes

- **NEW**: BPMN instance viewer component - visualizes running BPMN process instances with current state
- **NEW**: CMMN instance viewer component - visualizes running CMMN case instances with current state
- **NEW**: Instance navigation service - provides diagram XML and current state data for rendering
- **NEW**: Frontend integration with bpmn-js and cmmn-js libraries
- **MODIFY**: Instance detail API responses to include diagram XML and active element IDs
- **MODIFY**: Frontend instance detail views to include diagram visualization

## Capabilities

### New Capabilities
- `bpmn-instance-viewer`: Provides interactive visualization of running BPMN process instances, showing active user tasks, completed activities, and current process state with zoom/pan navigation
- `cmmn-instance-viewer`: Provides interactive visualization of running CMMN case instances, showing active plan items, completed stages, and current case state with zoom/pan navigation
- `instance-diagram-data`: API service that returns diagram XML and current state (active element IDs, completed element IDs) for any running instance

### Modified Capabilities
None (new feature only)

## Impact

**Backend:**
- New API endpoints to fetch diagram XML and state data for instances
- InstanceDTO and related controllers may need diagram XML inclusion

**Frontend:**
- New dependencies: bpmn-js and cmmn-js npm packages
- New React components for diagram rendering
- Integration with existing instance detail views

**Data:**
- Process/case definitions already include diagram XML from Flowable engines
- No database migrations required (diagram stored with definitions)
