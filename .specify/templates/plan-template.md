# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Java 21, TypeScript 5+
**Primary Dependencies**: Spring Boot 3.5.x, Flowable 7.x, Next.js (App Router), React 18+
**Storage**: PostgreSQL 15+ (multi-tenant schema strategy)
**Testing**: JUnit 5, Testcontainers, Jest, React Testing Library
**Target Platform**: Linux server (backend), Modern browsers (frontend)
**Project Type**: Web service (multi-tenant workflow platform)
**Performance Goals**: <500ms p95 response time, 1000+ concurrent users
**Constraints**: Multi-tenant data isolation, Flowable-native services, Server/Client Component separation
**Scale/Scope**: Multi-tenant SaaS, 10k+ users, 50+ process definitions

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [ ] **Multi-Tenant Isolation**: Does design maintain absolute tenant data separation?
- [ ] **Flowable-Native**: Are we using TaskService/RuntimeService/HistoryService directly (no abstractions)?
- [ ] **Server/Client Boundaries**: Are all data fetching and Flowable calls in Server Components only?
- [ ] **Form-Process Binding**: Do SurveyJS forms map correctly to Flowable process variables?
- [ ] **Test Coverage**: Are there integration tests for all workflow gateways and decision points?
- [ ] **Audit Trail**: Is every workflow action logged with userId, tenantId, timestamp, IP?
- [ ] **Performance**: Do database queries include tenant_id filtering?
- [ ] **Security**: Are process variables encrypted for sensitive data?

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)
src/
├── models/
├── services/
├── cli/
└── lib/

tests/
├── contract/
├── integration/
└── unit/

# [REMOVE IF UNUSED] Option 2: Web application (when "frontend" + "backend" detected)
backend/
├── src/
│   ├── models/
│   ├── services/
│   └── api/
└── tests/

frontend/
├── src/
│   ├── components/
│   ├── pages/
│   └── services/
└── tests/

# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android" detected)
api/
└── [same as backend above]

ios/ or android/
└── [platform-specific structure: feature modules, UI flows, platform tests]
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
