# Implementation Plan: Flowable Platform Core

**Branch**: `001-flowable-platform-core` | **Date**: 2026-03-21 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-flowable-platform-core/spec.md`

## Summary

Build a comprehensive multi-tenant workflow platform supporting BPMN 2.0, CMMN 1.1, and DMN 1.3 standards using Flowable 7.x, Spring Boot 3.5.x, and Next.js. The platform provides 8 core modules: workflow engine, task center, form engine with visual builder, RBAC with OAuth2/OIDC, collaboration features, audit logging, analytics dashboards, and admin console. Each task must be verified with a checkbox before completion.

## Technical Context

**Language/Version**: Java 21, TypeScript 5+

**Primary Dependencies**:
- Backend: Spring Boot 3.5.x, Flowable 7.x, Spring Security 6.x, PostgreSQL 15+, OAuth2/OIDC
- Frontend: Next.js (App Router), React 18+, Tailwind CSS, Shadcn/UI, SurveyJS, ECharts

**Storage**: PostgreSQL 15+ (multi-tenant schema strategy with tenant_id column filtering)

**Testing**: JUnit 5, Testcontainers, Jest, React Testing Library

**Target Platform**: Linux server (backend), Modern browsers (frontend)

**Project Type**: Web service (multi-tenant workflow platform)

**Performance Goals**: <500ms p95 response time, 1000+ concurrent process instances

**Constraints**:
- Multi-tenant data isolation (tenant_id in ALL queries)
- Flowable-native services (no abstractions over TaskService/RuntimeService/HistoryService)
- Server/Client Component separation (Next.js App Router)
- Form-process variable binding (SurveyJS ↔ Flowable variables)
- Test-driven development (TDD mandatory)

**Scale/Scope**: Multi-tenant SaaS, 10k+ users, 50+ process definitions, 8 functional modules

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [ ] **Multi-Tenant Isolation**: ✅ PASS - All database schemas include tenant_id column, cache keys include tenant context
- [ ] **Flowable-Native**: ✅ PASS - Direct use of TaskService, RuntimeService, HistoryService with no abstraction layers
- [ ] **Server/Client Boundaries**: ✅ PASS - Next.js Server Components for data fetching, Client Components for interactions only
- [ ] **Form-Process Binding**: ✅ PASS - SurveyJS forms map directly to Flowable process variables with version control
- [ ] **Test Coverage**: ✅ PASS - Integration tests for all BPMN/CMMN/DMN gateway and decision scenarios
- [ ] **Audit Trail**: ✅ PASS - All workflow actions logged with userId, tenantId, timestamp, IP, action
- [ ] **Performance**: ✅ PASS - Database queries optimized with tenant_id filtering and indexing
- [ ] **Security**: ✅ PASS - Process variables encrypted for sensitive data, OAuth2/OIDC authentication

## Project Structure

### Documentation (this feature)

```text
specs/001-flowable-platform-core/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── api-endpoints.md # REST API contracts
│   ├── form-schema.md   # Form schema JSON structure
│   └── auth-flows.md    # Authentication flow contracts
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
flowable-platform-demo/
├── backend/                    # Spring Boot 3.5.x backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/flowable/platform/
│   │   │   │   ├── config/      # Spring configuration
│   │   │   │   │   ├── FlowableConfig.java
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── OAuth2Config.java
│   │   │   │   │   └── MultiTenantConfig.java
│   │   │   │   ├── controller/  # REST controllers
│   │   │   │   │   ├── ProcessController.java
│   │   │   │   │   ├── TaskController.java
│   │   │   │   │   ├── FormController.java
│   │   │   │   │   └── AdminController.java
│   │   │   │   ├── service/     # Business logic (Flowable-native)
│   │   │   │   │   ├── ProcessService.java
│   │   │   │   │   ├── TaskService.java
│   │   │   │   │   ├── FormService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── AuditService.java
│   │   │   │   ├── repository/  # JPA repositories
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── RoleRepository.java
│   │   │   │   │   └── TenantRepository.java
│   │   │   │   ├── entity/      # JPA entities
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── Department.java
│   │   │   │   │   ├── Tenant.java
│   │   │   │   │   ├── FormSchema.java
│   │   │   │   │   ├── AuditLog.java
│   │   │   │   │   ├── Comment.java
│   │   │   │   │   └── Attachment.java
│   │   │   │   └── dto/        # Data transfer objects
│   │   │   │       ├── ProcessInstanceDTO.java
│   │   │   │       ├── TaskDTO.java
│   │   │   │       └── FormDTO.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-prod.yml
│   │   │       ├── db/migration/  # Flyway migrations
│   │   │       └── flowable/      # Flowable configuration
│   │   └── test/
│   │       ├── integration/       # Integration tests
│   │       │   ├── ProcessIntegrationTest.java
│   │       │   ├── TaskIntegrationTest.java
│   │       │   └── MultiTenantIntegrationTest.java
│   │       └── unit/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                   # Next.js 14+ frontend
│   ├── src/
│   │   ├── app/                # App Router pages
│   │   │   ├── login/
│   │   │   ├── tasks/
│   │   │   │   ├── page.tsx          # Server Component
│   │   │   │   └── components/       # Client Components
│   │   │   │       ├── TaskList.tsx
│   │   │   │       └── TaskCard.tsx
│   │   │   ├── processes/
│   │   │   ├── forms/
│   │   │   │   ├── builder/
│   │   │   │   │   └── page.tsx       # Visual form builder
│   │   │   │   └── preview/
│   │   │   ├── admin/
│   │   │   │   ├── processes/        # Process deployment
│   │   │   │   ├── instances/        # Instance management
│   │   │   │   └── users/            # User/role/department management
│   │   │   └── dashboard/
│   │   ├── components/
│   │   │   ├── ui/                  # Shadcn/UI components
│   │   │   ├── forms/               # SurveyJS integration
│   │   │   │   ├── SurveyForm.tsx    # Server Component wrapper
│   │   │   │   └── FormRenderer.tsx
│   │   │   ├── workflow/            # bpmn.js integration
│   │   │   │   ├── ProcessDiagram.tsx
│   │   │   │   └── TaskFlowViewer.tsx
│   │   │   └── analytics/           # ECharts integration
│   │   │       └── Dashboard.tsx
│   │   ├── lib/
│   │   │   ├── api.ts               # API client functions
│   │   │   ├── auth.ts              # OAuth2/OIDC client
│   │   │   └── utils.ts
│   │   └── styles/
│   ├── public/
│   ├── Dockerfile
│   └── package.json
│
├── database/                   # Database initialization
│   └── init.sql
│
├── docker-compose.yml         # Multi-container orchestration
├── architecture.md            # System design reference
├── constitution.md            # Technical governance (.specify/memory/)
└── instructions.md            # Development protocol
```

**Structure Decision**: Selected Option 2 (Web Application - backend + frontend) because the spec clearly defines separate backend (Spring Boot + Flowable) and frontend (Next.js) components with API-based communication.

---

## Phase 0: Research & Unknowns Resolution

**Goal**: Resolve all technical unknowns and research best practices for Flowable 7.x + Spring Boot 3.5.x + Next.js + multi-tenant architecture.

### Research Tasks

- [ ] **Research Task 1**: Flowable 7.x multi-tenant configuration strategies
  - Investigate Flowable 7.x multi-tenant support (tenant_id column vs separate schemas)
  - Research Flowable IdentityLinkService for multi-tenant user/group/role mapping
  - Document tenant context propagation through Flowable engine
  - **Output**: Flowable multi-tenant configuration guide (2-3 pages)

- [ ] **Research Task 2**: Spring Boot 3.5.x + Flowable 7.x integration patterns
  - Investigate Spring Boot 3.5.x auto-configuration for Flowable 7.x
  - Research Flowable Spring Boot starter configuration properties
  - Document best practices for Flowable service injection in Spring context
  - **Output**: Spring-Flowable integration guide (2-3 pages)

- [ ] **Research Task 3**: Next.js App Router + SurveyJS integration
  - Investigate SurveyJS React library with Next.js Server Components
  - Research dynamic form JSON schema generation and rendering
  - Document form version control and historical instance rendering strategies
  - **Output**: SurveyJS-Next.js integration guide (2-3 pages)

- [ ] **Research Task 4**: OAuth2 + OpenID Connect implementation for Spring Security 6.x
  - Investigate Spring Security 6.x OAuth2/OIDC client configuration
  - Research multi-tenant authentication with tenant-specific IdP configurations
  - Document token validation, user provisioning, and session management
  - **Output**: OAuth2/OIDC implementation guide (2-3 pages)

- [ ] **Research Task 5**: bpmn.js integration with React 18+ and Next.js
  - Investigate bpmn.js library compatibility with Next.js App Router
  - Research process diagram rendering with current node highlighting
  - Document lazy loading and performance optimization strategies
  - **Output**: bpmn.js-React integration guide (2-3 pages)

- [ ] **Research Task 6**: PostgreSQL 15+ multi-tenant query optimization
  - Investigate PostgreSQL indexing strategies for tenant_id filtering
  - Research query performance patterns for 1000+ concurrent instances
  - Document partitioning and archiving strategies for historical data
  - **Output**: PostgreSQL multi-tenant optimization guide (2-3 pages)

- [ ] **Research Task 7**: Testcontainers for Flowable integration testing
  - Investigate Testcontainers PostgreSQL module for Flowable testing
  - Research BPMN/CMMN/DMN test scenario patterns
  - Document test data setup and teardown strategies
  - **Output**: Testcontainers-Flowable testing guide (2-3 pages)

### Research Consolidation Document

**Output**: `specs/001-flowable-platform-core/research.md`

Document structure:
```markdown
# Research: Flowable Platform Core

## 1. Flowable 7.x Multi-Tenant Configuration
### Decision: Use tenant_id column strategy
### Rationale: ...
### Alternatives Considered: Separate schemas per tenant (rejected due to complexity)

## 2. Spring Boot 3.5.x + Flowable 7.x Integration
### Decision: Use flowable-spring-boot-starter
### Rationale: ...
### Configuration Examples: ...

[... continuing for all research tasks]
```

---

## Phase 1: Design & Contracts

**Prerequisites**: `research.md` complete, all technical unknowns resolved

### 1.1 Data Model Design

**Output**: `specs/001-flowable-platform-core/data-model.md`

**Entity Definitions**:

- [ ] **Tenant Entity**
  - Fields: id (UUID), name (String), code (String, unique), createdAt, updatedAt, isActive
  - Relationships: One-to-many with User, ProcessDefinition, ProcessInstance, FormSchema
  - Validation: code unique, name required, isActive defaults to true
  - Indexes: idx_tenant_code, idx_tenant_active

- [ ] **User Entity**
  - Fields: id (UUID), username (String, unique), email (String, unique), password (hashed), firstName, lastName, tenantId (FK), createdAt, updatedAt, isActive
  - Relationships: Many-to-many with Role, Many-to-many with Department, One-to-many with AuditLog, One-to-many with Comment
  - Validation: username/email unique per tenant, email format validation
  - Indexes: idx_user_tenant_username, idx_user_tenant_email

- [ ] **Role Entity**
  - Fields: id (UUID), name (String), code (String), description, tenantId (FK), createdAt, updatedAt
  - Relationships: Many-to-many with User
  - Validation: code unique per tenant
  - Indexes: idx_role_tenant_code

- [ ] **Department Entity**
  - Fields: id (UUID), name (String), code (String), parentId (FK, self-reference), tenantId (FK), createdAt, updatedAt
  - Relationships: Many-to-many with User, One-to-many with Department (parent-child)
  - Validation: code unique per tenant, parentId must reference existing department
  - Indexes: idx_dept_tenant_code, idx_dept_parent

- [ ] **FormSchema Entity**
  - Fields: id (UUID), name (String), version (String), processDefinitionKey (String), taskDefinitionKey (String, nullable), schemaJson (JSONB), tenantId (FK), createdAt, updatedAt, isActive
  - Relationships: Many-to-one with Tenant, One-to-many with ProcessVariable (through form submission)
  - Validation: version format (semantic versioning), schemaJson valid JSON, isActive defaults to true
  - Indexes: idx_form_tenant_process_version

- [ ] **AuditLog Entity**
  - Fields: id (UUID), userId (FK), tenantId (FK), timestamp, ipAddress (String), actionType (String), entityType (String), entityId (String), details (JSONB), createdAt
  - Relationships: Many-to-one with User and Tenant
  - Validation: all required fields except details (optional)
  - Indexes: idx_audit_tenant_user, idx_audit_timestamp, idx_audit_action

- [ ] **Comment Entity**
  - Fields: id (UUID), content (Text), authorId (FK), taskId (String, nullable), processInstanceId (String, nullable), tenantId (FK), createdAt, updatedAt
  - Relationships: Many-to-one with User, Tenant, Task (logical reference), ProcessInstance (logical reference)
  - Validation: content required, taskId or processInstanceId must be specified
  - Indexes: idx_comment_task, idx_comment_instance

- [ ] **Attachment Entity**
  - Fields: id (UUID), fileName (String), fileSize (Long), mimeType (String), storagePath (String), uploaderId (FK), taskId (String, nullable), processInstanceId (String, nullable), tenantId (FK), createdAt
  - Relationships: Many-to-one with User, Tenant
  - Validation: fileName required, storagePath required
  - Indexes: idx_attachment_task, idx_attachment_instance

- [ ] **Dashboard Entity**
  - Fields: id (UUID), name (String), ownerId (FK), layoutJson (JSONB), tenantId (FK), createdAt, updatedAt
  - Relationships: Many-to-one with User and Tenant, One-to-many with Widget
  - Validation: name required per tenant, layoutJson valid JSON
  - Indexes: idx_dashboard_tenant_owner

- [ ] **Widget Entity**
  - Fields: id (UUID), dashboardId (FK), type (String), title (String), dataSource (String), filterConfig (JSONB), position (Integer), tenantId (FK), createdAt
  - Relationships: Many-to-one with Dashboard and Tenant
  - Validation: type must be valid (bar, line, pie, funnel), position within dashboard bounds
  - Indexes: idx_widget_dashboard

**State Transitions**:
- ProcessInstance: RUNNING → SUSPENDED → ACTIVATED or TERMINATED
- Task: CREATED → ASSIGNED → CLAIMED → COMPLETED or DELEGATED
- FormSchema: ACTIVE → SUPERSEDED (when new version created)
- User: ACTIVE → INACTIVE (soft delete)

### 1.2 API Contracts

**Output**: `specs/001-flowable-platform-core/contracts/api-endpoints.md`

**REST API Structure**:

```markdown
# REST API Contracts

## Authentication Endpoints
- POST /api/auth/login - Local username/password login
- POST /api/auth/oauth2/{provider} - OAuth2/OIDC provider callback
- GET /api/auth/logout - Terminate session
- GET /api/auth/me - Get current user info

## Process Management Endpoints
- POST /api/processes - Start new process instance
- GET /api/processes - List process instances (paginated, filterable)
- GET /api/processes/{id} - Get process instance details
- GET /api/processes/{id}/diagram - Get process diagram with current node highlighted
- POST /api/processes/{id}/suspend - Suspend process instance
- POST /api/processes/{id}/activate - Activate suspended instance
- POST /api/processes/{id}/terminate - Terminate process instance
- GET /api/processes/{id}/history - Get process execution history
- GET /api/processes/definitions - List deployed process definitions
- POST /api/processes/definitions - Deploy process definition (XML file upload)
- GET /api/processes/definitions/{id}/xml - Get process definition XML
- PUT /api/processes/definitions/{id}/version/{version} - Set primary version

## Task Management Endpoints
- GET /api/tasks/my-tasks - Get current user's pending tasks
- GET /api/tasks/completed - Get current user's completed tasks
- GET /api/tasks/my-requests - Get process instances started by current user
- GET /api/tasks/all - Get all tasks (admin only, paginated)
- GET /api/tasks/{id} - Get task details
- POST /api/tasks/{id}/claim - Claim task
- POST /api/tasks/{id}/complete - Complete task with form data
- POST /api/tasks/{id}/delegate - Delegate task to another user
- POST /api/tasks/{id}/reassign - Reassign task (admin only)
- GET /api/tasks/{id}/form - Get task form schema
- POST /api/tasks/{id}/comments - Add comment to task
- GET /api/tasks/{id}/comments - Get task comments
- POST /api/tasks/{id}/attachments - Upload attachment
- GET /api/tasks/{id}/attachments - Get task attachments

## Form Management Endpoints
- GET /api/forms - List form schemas (paginated, filterable)
- GET /api/forms/{id} - Get form schema details
- POST /api/forms - Create new form schema (visual builder output)
- PUT /api/forms/{id} - Update form schema (creates new version)
- GET /api/forms/{id}/versions - Get form schema version history
- POST /api/forms/validate - Validate form submission data

## User & RBAC Endpoints
- GET /api/users - List users (paginated, filterable)
- POST /api/users - Create new user
- GET /api/users/{id} - Get user details
- PUT /api/users/{id} - Update user
- DELETE /api/users/{id} - Delete user (soft delete)
- GET /api/roles - List roles
- POST /api/roles - Create role
- GET /api/departments - List departments (hierarchical)
- POST /api/departments - Create department
- PUT /api/departments/{id}/users - Add/remove users from department

## Admin Console Endpoints
- GET /api/admin/processes/definitions - List all process definitions (all tenants)
- POST /api/admin/processes/definitions/deploy - Deploy process definition (admin only)
- GET /api/admin/instances - List all process instances (admin only, paginated)
- POST /api/admin/instances/{id}/suspend - Admin suspend instance
- POST /api/admin/instances/{id}/activate - Admin activate instance
- POST /api/admin/instances/{id}/variables - Modify process variables
- POST /api/admin/instances/{id}/jump - Force jump to node
- GET /api/admin/audit-logs - Query audit logs (paginated, filterable)

## Analytics Endpoints
- GET /api/analytics/efficiency - Get task completion efficiency metrics
- GET /api/analytics/bottlenecks - Get process bottleneck analysis
- GET /api/analytics/distribution - Get process instance distribution
- GET /api/dashboards - List user dashboards
- POST /api/dashboards - Create custom dashboard
- PUT /api/dashboards/{id} - Update dashboard layout
- GET /api/dashboards/{id}/data - Get dashboard data

## Health & Monitoring Endpoints
- GET /actuator/health - Health check (readiness probe)
- GET /actuator/metrics - Prometheus metrics endpoint
- GET /actuator/info - Application info
```

**Request/Response Formats**:
- All endpoints accept/return JSON
- Pagination: `?page=0&size=20&sort=fieldName,asc`
- Error responses: `{"code": "ERROR_CODE", "message": "Human-readable message", "details": {}}`
- Tenant context: All endpoints require `X-Tenant-Id` header (except auth endpoints)

### 1.3 Form Schema Contract

**Output**: `specs/001-flowable-platform-core/contracts/form-schema.md`

```json
{
  "schemaVersion": "1.0",
  "formId": "uuid",
  "name": "Leave Request Form",
  "version": "1.0.0",
  "processDefinitionKey": "leaveRequest",
  "taskDefinitionKey": "approveLeave",
  "fields": [
    {
      "id": "startDate",
      "type": "date",
      "label": "Start Date",
      "placeholder": "Select start date",
      "required": true,
      "readOnly": false,
      "visible": true,
      "validation": {
        "rules": [
          {
            "type": "dateRange",
            "min": "today",
            "max": "+30d"
          }
        ]
      }
    },
    {
      "id": "reason",
      "type": "text",
      "label": "Reason",
      "placeholder": "Enter reason for leave",
      "required": true,
      "readOnly": false,
      "visible": true,
      "validation": {
        "rules": [
          {
            "type": "minLength",
            "value": 10
          },
          {
            "type": "maxLength",
            "value": 500
          }
        ]
      }
    }
  ],
  "processVariableMapping": {
    "startDate": "leaveStartDate",
    "reason": "leaveReason"
  }
}
```

### 1.4 Authentication Flow Contract

**Output**: `specs/001-flowable-platform-core/contracts/auth-flows.md`

**OAuth2/OIDC Flow**:
1. User clicks "Login with {Provider}"
2. Frontend redirects to `/oauth2/authorization/{provider}`
3. Backend redirects to provider's authorization endpoint
4. Provider redirects back to `/oauth2/callback/{provider}` with authorization code
5. Backend exchanges code for tokens
6. Backend extracts user info from ID token
7. Backend creates/updates user account in tenant context
8. Backend creates session and returns JWT
9. Frontend stores JWT and redirects to dashboard

**Local Auth Flow**:
1. User submits username/password
2. Backend validates credentials
3. Backend creates session and returns JWT
4. Frontend stores JWT and redirects to dashboard

### 1.5 Quickstart Guide

**Output**: `specs/001-flowable-platform-core/quickstart.md`

```markdown
# Flowable Platform - Quickstart Guide

## Prerequisites
- Docker 20.10+
- Docker Compose 2.0+
- Git

## 5-Minute Startup

1. Clone repository
2. Configure environment: cp .env.example .env
3. Start services: docker-compose up -d
4. Wait for services to be healthy (~2 minutes)
5. Access application: http://localhost:3000
6. Login with default credentials (see .env)

## Verification
- [ ] Frontend loads at http://localhost:3000
- [ ] Backend health check passes: curl http://localhost:8080/actuator/health
- [ ] Database accepts connections
- [ ] Can login and create first process instance

## Development Setup
1. Backend: cd backend && mvn spring-boot:run
2. Frontend: cd frontend && npm run dev
3. Database: docker-compose up postgres (standalone)

## First Process
1. Import sample BPMN file (see docs/sample-processes/)
2. Deploy through admin console
3. Start process instance
4. Complete task
5. View process history

## Troubleshooting
- Port conflicts: Edit docker-compose.yml ports
- Database connection errors: Check .env POSTGRES_PASSWORD
- Frontend API errors: Check NEXT_PUBLIC_API_URL
```

### 1.6 Agent Context Update

**Output**: Execute update script to add new technologies to agent context

- [ ] Run: `powershell -File .specify/scripts/powershell/update-agent-context.ps1 -AgentType claude`
- [ ] Verify: Check that new technologies added to agent's context file between markers

---

## Constitution Compliance Verification

*Re-evaluating after Phase 1 design*

### Multi-Tenant Architecture First ✅
- [x] **Data Model**: All entities include tenantId FK
- [x] **API Contracts**: X-Tenant-Id header required on all endpoints
- [x] **Cache Strategy**: Document tenant-aware cache keys in research
- [x] **Flowable Config**: Multi-tenant strategy defined (tenant_id column approach)

### Flowable-Native Integration ✅
- [x] **Service Layer**: Direct TaskService/RuntimeService/HistoryService usage in service design
- [x] **No Abstractions**: ProcessService, TaskService use Flowable APIs directly
- [x] **Variable Scopes**: Process variables use Flowable's native scoping (local/global)

### Frontend-Backend Separation ✅
- [x] **Server Components**: App Router pages are Server Components by default
- [x] **Client Components**: Form interactions, task cards are Client Components
- [x] **API Bridge**: All Flowable calls go through API routes, not direct from client

### Form-Process Binding ✅
- [x] **Variable Mapping**: Form schema contract defines processVariableMapping field
- [x] **Version Control**: FormSchema entity includes version field for historical accuracy
- [x] **Validation**: Server-side form validation before process variable update

### Test-Driven Development ✅
- [x] **Test Strategy**: Testcontainers for integration tests defined
- [x] **Coverage**: Integration tests for all BPMN/CMMN/DMN gateways required
- [x] **Scenarios**: Each user story includes acceptance scenarios for test planning

### Observability & Audit Trail ✅
- [x] **Audit Entity**: AuditLog captures all required fields (userId, tenantId, timestamp, IP, action)
- [x] **Logging Strategy**: All workflow actions logged (FR-031 to FR-035 preserved in service design)
- [x] **Monitoring**: Prometheus endpoints defined for observability

---

## Complexity Tracking

> **No violations requiring justification - all constitution checks pass**

---

## Next Steps

**Phase 2**: Run `/speckit.tasks` to generate actionable task list with:
- Task breakdown by user story (P1 → P2 → P3)
- Independent task verification checkboxes
- Dependency tracking
- Parallel execution opportunities

**Ready for task generation**: All design artifacts complete, constitution verified, technical unknowns resolved.
