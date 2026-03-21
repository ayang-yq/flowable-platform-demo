<!--
=============================================================================
SYNC IMPACT REPORT - Constitution v1.0.0
=============================================================================
Version Change: [INITIAL] → 1.0.0
Date: 2026-03-21

PRINCIPLES ESTABLISHED:
✅ I. Multi-Tenant Architecture First (NON-NEGOTIABLE)
✅ II. Flowable-Native Integration (NON-NEGOTIABLE)
✅ III. Frontend-Backend Separation Discipline
✅ IV. Form-Process Binding Protocol
✅ V. Test-Driven Development (MANDATORY)
✅ VI. Observability & Audit Trail (NON-NEGOTIABLE)

SECTIONS ADDED:
✅ Strict Constraints (A-E): Technology stack, security, Flowable rules, frontend limits, performance
✅ Development Workflow: Quality gates, deployment, incident response
✅ Governance: Amendment process, compliance, template synchronization

TEMPLATE UPDATES COMPLETED:
✅ .specify/templates/plan-template.md - Added Flowable-specific constitution checks
✅ .specify/templates/spec-template.md - Added workflow and multi-tenant requirement sections
✅ .specify/templates/tasks-template.md - Updated setup/foundational phases for Flowable platform

FOLLOW-UP TODOs:
⚠️ Review and update command-specific guidance in .specify/templates/commands/*.md
⚠️ Verify README.md and quickstart docs reference new constitution principles
⚠️ Consider creating separate development-guide.md with runtime workflow examples

RATIONALE FOR v1.0.0 (MAJOR):
Initial constitution ratification based on architecture.md requirements.
Establishes core governance for multi-tenant Flowable platform development.
=============================================================================
-->

# Flowable Platform Constitution

## Core Principles

### I. Multi-Tenant Architecture First (NON-NEGOTIABLE)
**Absolute data isolation is the foundation of this platform.**
- Every database query MUST include tenant_id filtering - no exceptions
- Tenant context must be propagated through all layers: HTTP request → Service → Repository → Database
- Flowable engine MUST be configured with multi-tenant schema strategy (separate schemas per tenant or tenant_id column)
- Cross-tenant data access is strictly prohibited, even for admin operations
- All cache keys MUST include tenant identifier to prevent cache poisoning

**Rationale**: Data leakage between tenants is an existential risk. Multi-tenancy is not a feature to add later—it must be architected into every layer from day one.

### II. Flowable-Native Integration (NON-NEGOTIABLE)
**Use Flowable as intended - never wrap, mock, or abstract core services.**
- Direct use of TaskService, RuntimeService, HistoryService, IdentityService, and RepositoryService
- No custom abstraction layers over Flowable APIs - use native services directly
- All process variable operations must use Flowable's native variable scopes (local, global, execution-local)
- BPMN 2.0, CMMN 1.1, and DMN 1.3 files MUST be valid per specifications - use Flowable Modeler or compatible tools
- Never bypass Flowable's transaction management - engine-managed transactions only

**Rationale**: Abstracting Flowable creates maintenance debt and limits engine capabilities. Native service access ensures we leverage Flowable's full power and stay compatible with upgrades.

### III. Frontend-Backend Separation Discipline
**Next.js App Router requires strict Server/Client Component boundaries.**
- Server Components (default): Data fetching, database queries, Flowable service calls, secret management
- Client Components (explicit 'use client'): User interactions, form inputs, real-time updates, browser APIs
- API routes are the ONLY bridge between Client Components and backend services
- No direct database connections, Flowable services, or secrets in Client Components
- All Server Components must pass serializable data to Client Components (no functions, classes, or complex objects)

**Rationale**: Server Components provide security (secrets stay server-side) and performance (direct database access). Client Components enable interactivity. Mixing these concerns breaks Next.js architecture.

### IV. Form-Process Binding Protocol
**SurveyJS forms are the interface to Flowable process variables.**
- Every form submission MUST map to a Flowable process variable (use camelCase for variable names)
- Form JSON schema must include metadata: formVersion, processDefinitionKey, taskDefinitionKey
- Form field permissions (readonly/required/hidden) MUST be evaluated server-side before rendering
- Historical process instances must render with the exact form version active at completion time
- Form validation occurs in SurveyJS (client) AND server-side (before process variable update)

**Rationale**: Forms are the user-facing layer of process variables. Strict versioning and validation ensure process data integrity and historical accuracy.

### V. Test-Driven Development (MANDATORY)
**TDD is not optional - it is the development workflow.**
- Red phase: Write failing test before any implementation code
- Green phase: Implement minimum code to pass the test
- Refactor phase: Improve design while tests remain green
- Test categories: Unit tests (logic), Integration tests (services + Flowable), E2E tests (full workflows)
- All BPMN/CMMN/DMN files must have accompanying test scenarios covering all gateways and decision points

**Rationale**: Flowable workflows are complex state machines. TDD ensures process definitions behave as designed and prevents regression when processes evolve.

### VI. Observability & Audit Trail (NON-NEGOTIABLE)
**Every workflow action must be traceable and auditable.**
- All user actions (start process, complete task, claim task, delegate) must log: userId, tenantId, timestamp, IP address, action details
- Audit log must be immutable - use append-only storage
- Flowable's native history tables are the source of truth - build queries on HistoryService, not custom tables
- Process visualization must use actual historical data from Flowable, not reconstructed state
- SLA breaches must trigger alerts and be recorded in audit log

**Rationale**: Workflow platforms are critical business infrastructure. Audit trails are required for compliance, debugging, and user accountability.

## Strict Constraints

### A. Technology Stack Mandates
- **Backend**: Spring Boot 3.5.x, Java 21, Spring Security 6.x, Flowable 7.x (BPMN, CMMN, DMN)
- **Database**: PostgreSQL 15+ - no other databases supported
- **Frontend**: Next.js (App Router), React 18+, Tailwind CSS, Shadcn/UI, SurveyJS, ECharts
- **Documentation Store**: Third-party document management system via API (no local file storage)
- **Violating these versions requires architecture committee approval**

### B. Security Constraints
- All API endpoints must require authentication - no public exceptions except login/forgot-password
- Role-Based Access Control (RBAC) checks must occur at service layer, not just UI
- Process variable encryption for sensitive data (PII, financial, health data)
- Tenant isolation must be enforced at database row level - never filter in application code only
- CSRF protection mandatory for all state-changing operations
- Rate limiting on all public APIs

### C. Flowable-Specific Constraints
- NEVER mock TaskService, RuntimeService, or HistoryService in production code
- All process definition deployments must be versioned - never overwrite existing deployments
- Process variables must be primitives or JSON-serializable objects - no Java objects
- Suspend/activate operations must use Flowable's native suspension API
- Custom SQL queries on Flowable tables are prohibited - use Flowable services only
- Process instance deletion requires admin approval and audit logging

### D. Frontend Constraints
- No client-side Flowable API calls - all engine operations go through API routes
- SurveyJS forms must be server-rendered for security (form structure determined by server permissions)
- Real-time updates use Server-Sent Events (SSE) or WebSockets - no polling
- All file uploads go through backend validation before storage
- Client-side pagination is prohibited - server-side pagination only

### E. Performance & Scalability
- Database connection pooling configured for tenant isolation (HikariCP with tenant-aware pools)
- Process variable payload must not exceed 10KB per variable - use document store for large data
- History data older than 2 years must be archived - do not delete, archive to cold storage
- Cache invalidation must be tenant-aware - never cache tenant data in global scope
- API response time p95 < 500ms for all operations (except bulk operations)

## Development Workflow

### Code Quality Gates
- All code must pass static analysis (SpotBugs for Java, ESLint for TypeScript)
- Test coverage minimum: 80% for services, 60% for UI components
- Pull request approval required from at least one senior architect
- All process definition changes require test suite update

### Deployment Discipline
- Database migrations must use Flyway or Liquibase - no manual schema changes
- Flowable process deployments require automated rollback plan
- Zero-downtime deployment strategy: Blue-green deployment for backend, Next.js incremental static regeneration for frontend
- Feature flags required for all new major features
- Smoke tests must run post-deployment before traffic is fully routed

### Incident Response
- All production incidents require post-mortem document
- Process instance corruption triggers automatic alert to architecture team
- SLA breaches trigger escalation notification
- Emergency process termination requires audit trail and approval documentation

## Governance

### Amendment Process
1. Any team member can propose constitution changes via RFC (Request for Comments)
2. Architecture committee reviews and approves changes
3. Changes must be documented in constitution with version bump (MAJOR.MINOR.PATCH)
4. All dependent templates and guidance documents must be updated
5. Team training required for MAJOR version changes

### Compliance & Enforcement
- All pull requests must reference applicable constitution principles
- Automated linting checks enforce technical constraints
- Architecture committee reviews violations monthly
- Repeated non-compliance triggers additional training and review

### Template Synchronization
When this constitution changes, the following templates must be reviewed and updated:
- `.specify/templates/spec-template.md` - ensure requirements capture constitutional constraints
- `.specify/templates/plan-template.md` - verify constitution checks are included
- `.specify/templates/tasks-template.md` - update task categories for new requirements
- `.specify/templates/commands/*.md` - align command guidance with principles

**Version**: 1.0.0 | **Ratified**: 2026-03-21 | **Last Amended**: 2026-03-21