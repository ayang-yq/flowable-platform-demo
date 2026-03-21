# Feature Specification: Flowable Platform Core

**Feature Branch**: `001-flowable-platform-core`
**Created**: 2026-03-21
**Status**: Draft
**Input**: User description: "based on provided architecture.md, i would like to create a general flowable platform, the system can support CMMN, BPMN and DMN engine and process management, Task center, Form engine, RBAC & Identity module, Comments & Collaboration, Audit & Logging, report module including analytics and customized dashboards, admin console for model deployment, instance management"

## Clarifications

### Session 2026-03-21

- Q: Should the platform include a built-in process model designer, or should users design processes externally? → A: No built-in designer - users design processes externally using Flowable Design Cloud or open-source modelers, then import XML files to deploy and execute within the platform
- Q: What authentication strategy should the platform support? → A: OAuth2 + OpenID Connect with fallback to local username/password - support external identity providers (Azure AD, Okta, Google Workspace) while allowing simple deployments with local accounts
- Q: What observability and monitoring strategy should the platform implement? → A: Prometheus metrics + Grafana dashboards for system health, performance monitoring, and alerting
- Q: What system availability and reliability targets should the platform commit to? → A: No formal SLA - best effort availability with no guaranteed uptime targets (acceptable for internal platform, development/testing environments, or where strict business continuity guarantees are not required)
- Q: How should business analysts create form schemas without writing JSON directly? → A: Visual form builder with drag-and-drop interface - business analysts design forms visually, system generates JSON schemas automatically, developers can manually edit JSON if needed

### Out of Scope

The following capabilities are explicitly OUT OF SCOPE for this platform:

- **Process Model Designer**: No built-in visual BPMN/CMMN/DMN designer. Users must use external tools (Flowable Design Cloud, Camunda Modeler, other compliant modelers) to create process models before importing XML files.
- **Process Simulation**: No what-if scenario analysis or process simulation capabilities.
- **Process Mining**: No automated process discovery or optimization recommendations based on event logs.
- **Social Collaboration**: No social features like activity feeds, discussion forums, or ideation platforms beyond task-focused comments and mentions.
- **Mobile Applications**: No native mobile apps (web interface must be mobile-responsive).
- **API Gateway/Management**: No API gateway, rate limiting, or API monetization features (standard REST APIs provided only).
- **Business Rules Engine (separate)**: No standalone business rules management system beyond DMN decision table integration.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Core Workflow Process Management (Priority: P1)

Business users can import, deploy, and execute complex business processes using industry-standard workflow engines (BPMN for structured processes, CMMN for flexible case management, DMN for decision automation). Users design process models externally using specialized tools, then import XML files into the platform for deployment and execution. Users can start process instances, track progress through visual diagrams, and manage the complete lifecycle from initiation to completion.

**Why this priority**: This is the foundational capability that enables all other features. Without core workflow management, there is no platform.

**Independent Test**: Can be fully tested by importing a BPMN file for a simple approval process (e.g., leave request), deploying it, starting an instance, and verifying it progresses through defined nodes with proper status tracking.

**Acceptance Scenarios**:

1. **Given** a valid BPMN process model is deployed, **When** a user starts a new process instance, **Then** the system creates an instance with a unique ID and initial status "Running"
2. **Given** a process instance is running, **When** it reaches a user task node, **Then** the system creates a task assigned to the specified user/group
3. **Given** a process contains a DMN decision table, **When** execution reaches the decision point, **Then** the system evaluates the decision and routes to the correct next node
4. **Given** a CMMN case instance is active, **When** a user creates an ad-hoc task, **Then** the system adds the task to the case without disrupting the case plan
5. **Given** a process instance is running, **When** a user views the process diagram, **Then** current node is highlighted and completed nodes show visual history

---

### User Story 2 - Task Center Management (Priority: P1)

Users have a centralized workspace to manage all their workflow tasks. They can view pending tasks requiring their action, see their completed task history, and track process instances they initiated. Administrators can monitor all tasks across the organization for oversight and workload balancing.

**Why this priority**: Task management is the primary user interface for workflow execution. If users can't efficiently find and complete tasks, the workflow system fails its core purpose.

**Independent Test**: Can be fully tested by creating tasks for different users, logging in as each user, and verifying they see only their assigned tasks in appropriate queues (pending, completed, initiated).

**Acceptance Scenarios**:

1. **Given** a user has 5 assigned tasks, **When** they access "My Tasks", **Then** they see all 5 tasks sorted by priority/due date
2. **Given** a user completes a task, **When** they navigate to "My Completed Tasks", **Then** the completed task appears in their history
3. **Given** a user started a process instance, **When** they access "My Requests", **Then** they see the instance with current status and timeline
4. **Given** an administrator views "All Tasks", **When** filtering by department, **Then** only tasks from that department are displayed
5. **Given** a task is overdue, **When** any user views task lists, **Then** overdue tasks are visually highlighted

---

### User Story 3 - Dynamic Form Engine (Priority: P1)

Business analysts can create dynamic forms without coding that automatically bind to workflow process variables. Forms support versioning so historical process instances always render with the exact form version used when the instance was active. Field permissions (read-only, required, hidden) are automatically enforced based on the current workflow node.

**Why this priority**: Forms are the primary user interface for data collection in workflows. Without dynamic forms, every process change requires coding, making the system rigid and expensive to maintain.

**Independent Test**: Can be fully tested by creating a form schema, deploying it with a process, starting instances, modifying the form schema, and verifying old instances still use the original form while new instances use the updated form.

**Acceptance Scenarios**:

1. **Given** a form schema is deployed with a process, **When** a user reaches a task with that form, **Then** the form renders with all fields and appropriate permissions
2. **Given** a user submits a form, **When** the data is captured, **Then** all form fields are automatically mapped to process variables
3. **Given** a form schema is updated to version 2.0, **When** a process instance started with version 1.0 is viewed, **Then** it renders with version 1.0 of the form
4. **Given** a task is at the "Manager Approval" node, **When** the form renders, **Then** fields marked as "manager-only" are visible while "employee-only" fields are read-only
5. **Given** a form field is marked as required, **When** a user attempts submission without filling it, **Then** the system prevents submission and shows validation error

---

### User Story 4 - Multi-Tenant Identity and Access Control (Priority: P2)

System administrators can manage users, roles, and departments across multiple independent tenants. Each tenant's users, processes, and data are completely isolated from other tenants. Users can be assigned roles that determine their permissions across different workflow operations. Department hierarchies can be used for task assignment and approval routing.

**Why this priority**: Multi-tenancy is critical for SaaS deployment and data isolation. Without it, the platform cannot serve multiple organizations or ensure data security compliance.

**Independent Test**: Can be fully tested by creating two tenants, adding users to each, starting process instances in both tenants, and verifying users from Tenant A cannot see or access any data from Tenant B.

**Acceptance Scenarios**:

1. **Given** two tenants exist (Tenant A and Tenant B), **When** a user from Tenant A logs in, **Then** they see no data, users, or processes from Tenant B
2. **Given** a user is assigned the "Manager" role, **When** a process requires manager approval, **Then** the system correctly assigns the task to that user
3. **Given** a department hierarchy exists (Engineering → Backend Team), **When** a process routes to the Engineering department manager, **Then** the task is assigned to the correct manager
4. **Given** an administrator creates a new user, **When** the user is saved, **Then** they are automatically synced to the workflow engine for task assignment
5. **Given** a user belongs to multiple departments, **When** a task is assigned to any of their departments, **Then** the user can see and claim the task

---

### User Story 5 - Collaboration and Communication (Priority: P2)

Users can provide approval comments when completing tasks, attach supporting documents to processes and tasks, and mention other users to notify them of important information. All collaboration is tied to specific workflow nodes and preserved in the audit trail for complete transparency.

**Why this priority**: Workflow execution often requires communication and context. Without collaboration features, users resort to external communication channels, breaking the audit trail and reducing process visibility.

**Independent Test**: Can be fully tested by completing a task with a comment, attaching a document, mentioning another user, and verifying all collaboration data is preserved and viewable in the process history.

**Acceptance Scenarios**:

1. **Given** a user is completing an approval task, **When** they submit the form, **Then** the system requires an approval comment before allowing submission
2. **Given** a user attaches a document to a task, **When** another user views the task, **Then** they can download and view the attachment
3. **Given** a user mentions "@john.doe" in a comment, **When** the comment is saved, **Then** John Doe receives a notification
4. **Given** a process has 5 completed nodes, **When** a user views the process history, **Then** all comments and attachments from each node are displayed in chronological order
5. **Given** a task is delegated to another user, **When** the new user views the task, **Then** they see all previous comments and attachments

---

### User Story 6 - Administrative Process Control (Priority: P2)

Administrators can deploy new process models (BPMN, CMMN, DMN), manage multiple versions of the same process, and intervene in running process instances when exceptions occur. They can suspend, activate, terminate, or modify variables in running processes to resolve business exceptions without stopping the entire system.

**Why this priority**: Process management requires ongoing maintenance and exception handling. Without admin controls, stuck processes cannot be resolved without database manipulation, which is risky and requires technical expertise.

**Independent Test**: Can be fully tested by deploying a process model, starting an instance, suspending it from the admin console, modifying a variable, and resuming to verify the changes take effect.

**Acceptance Scenarios**:

1. **Given** a valid BPMN file, **When** an administrator uploads it through the admin console, **Then** the process is deployed and available for starting new instances
2. **Given** a process has version 1.0 and 2.0 deployed, **When** an administrator marks version 2.0 as the primary version, **Then** new instances start with version 2.0 while version 1.0 instances continue running
3. **Given** a process instance is stuck at a node, **When** an administrator suspends the instance, **Then** the instance stops processing and no timers or events trigger
4. **Given** a process variable has incorrect data causing routing errors, **When** an administrator modifies the variable value, **Then** the process can continue with corrected data
5. **Given** a process instance needs to skip a node, **When** an administrator forces execution to jump to a later node, **Then** the instance resumes from the new node and continues normally

---

### User Story 7 - Analytics and Performance Dashboards (Priority: P3)

Managers and executives can view pre-built and custom dashboards showing workflow performance metrics. They can analyze task completion rates, identify process bottlenecks, track user workloads, and monitor process distribution. Users can create custom dashboards by dragging and dropping chart widgets relevant to their role.

**Why this priority**: Analytics provide insights for continuous process improvement. Without dashboards, organizations cannot identify inefficiencies or measure the impact of process optimizations.

**Independent Test**: Can be fully tested by executing several process instances, completing tasks with varying durations, and verifying the analytics dashboards display accurate metrics and visualizations.

**Acceptance Scenarios**:

1. **Given** 100 process instances completed last month, **When** a manager views the Process Distribution chart, **Then** they see a breakdown of which process types were used most frequently
2. **Given** a process has 10 completed instances, **When** a manager views the Bottleneck Analysis, **Then** the system highlights the node with the longest average duration
3. **Given** a user completed 50 tasks this month, **When** they view their Efficiency Dashboard, **Then** they see their average completion time and on-time percentage
4. **Given** a manager wants a custom view, **When** they drag "Department Task Volume" and "My Overdue Tasks" widgets to a new dashboard, **Then** the system saves and displays the custom dashboard on next login
5. **Given** SLA is configured for 24-hour task completion, **When** tasks exceed SLA, **Then** the dashboard shows an alert with the count of overdue tasks

---

### User Story 8 - Comprehensive Audit and Compliance Logging (Priority: P3)

Every action in the workflow system is logged with complete context including user identity, timestamp, IP address, tenant ID, and action details. Audit logs are append-only and can never be modified or deleted. Historical process data can be archived to separate storage to maintain performance while preserving complete records for compliance and forensic analysis.

**Why this priority**: Audit trails are mandatory for regulatory compliance and security incident response. Without complete logging, organizations cannot investigate issues or demonstrate compliance to auditors.

**Independent Test**: Can be fully tested by performing various workflow actions (starting processes, completing tasks, delegating, terminating), then querying the audit log to verify every action is recorded with full context.

**Acceptance Scenarios**:

1. **Given** a user starts a process instance, **When** the action completes, **Then** the audit log records: user ID, timestamp, IP address, tenant ID, process ID, and action "PROCESS_STARTED"
2. **Given** a process instance is completed, **When** 6 months pass, **Then** the system automatically archives the instance and its variables to cold storage
3. **Given** a compliance officer needs an audit report, **When** they query for a specific date range and user, **Then** the system returns all actions with complete context
4. **Given** an administrator attempts to modify a historical audit log entry, **When** the system detects the modification attempt, **Then** it denies the action and logs the attempt
5. **Given** a security incident occurs, **When** investigators analyze the audit trail, **Then** they can reconstruct the complete sequence of events for any process instance

---

### Edge Cases

- What happens when a process definition is updated while instances are still running on the old version?
- How does the system handle concurrent task completion attempts by multiple users for the same task?
- What happens when a user is deleted while they have active tasks assigned?
- How does the system behave when a DMN decision table has no matching rules for input data?
- What happens when a process instance exceeds maximum variable size limits?
- How does the system handle circular delegation loops (User A delegates to B, B delegates to A)?
- What happens when a tenant is deleted with active process instances?
- How does the system behave when database connection fails during a critical state transition?
- What happens when a form schema references a process variable that doesn't exist?
- How does the system handle timezone differences when displaying task due dates to global users?
- What happens when OAuth2 identity provider is temporarily unavailable during user login?
- How does the system behave when Prometheus metrics endpoint is overwhelmed by scraping frequency?
- What happens when Grafana dashboards show alerts but operators are unavailable to respond?
- How does the system handle scheduled maintenance windows with active process instances?
- What happens to in-flight workflow tasks when the platform undergoes unexpected downtime?
- How does the system handle concurrent editing of the same form schema by multiple business analysts?
- What happens when a business analyst creates a form schema that references process variables that don't exist in any deployed process?

## Requirements *(mandatory)*

### Functional Requirements

#### Core Workflow Engine

- **FR-001**: System MUST support BPMN 2.0 process definitions with all common node types (start event, end event, user task, service task, gateway, subprocess, call activity)
- **FR-002**: System MUST support CMMN 1.1 case definitions with case tasks, milestones, and ad-hoc task creation
- **FR-003**: System MUST support DMN 1.3 decision tables with input/output data and decision rules
- **FR-004**: System MUST enable advanced workflow controls including add signatory, remove signatory, sequential/parallel countersigning, reject to initiator, reject to specific node, and withdraw before next node approval
- **FR-005**: System MUST support task delegation (temporary) and reassignment (permanent transfer of ownership)
- **FR-006**: System MUST visualize process execution with dynamic diagram rendering showing current node, completed path, and pending nodes
- **FR-007**: System MUST support subprocess and call activity nesting for complex workflow composition

#### Task Center

- **FR-008**: System MUST provide "My Tasks" showing all pending tasks assigned to the current user or their groups/roles
- **FR-009**: System MUST provide "My Completed Tasks" showing historical tasks the user has processed
- **FR-010**: System MUST provide "My Requests" showing all process instances initiated by the current user
- **FR-011**: System MUST provide administrative "All Tasks" view for global task monitoring across all users
- **FR-012**: System MUST support task expiration alerts with configurable SLA timeframes and automatic notification escalation
- **FR-013**: System MUST support ad-hoc task creation within CMMN case instances for unstructured work
- **FR-014**: System MUST support task CC (carbon copy) to inform users without assigning approval responsibility

#### Form Engine

- **FR-015**: System MUST render dynamic forms based on JSON schema without requiring code changes
- **FR-016**: System MUST support form versioning where form changes create new versions without affecting historical instances
- **FR-017**: System MUST render historical process instances with the exact form version active when the instance was started
- **FR-018**: System MUST support global forms (one form per process) and node-specific forms (different forms per approval node)
- **FR-019**: System MUST enforce field-level permissions (read-only, required, hidden) based on current workflow node and user roles
- **FR-020**: System MUST automatically map form submission data to process variables using JSON serialization
- **FR-021**: System MUST provide visual form builder interface with drag-and-drop form field palette for business analysts to create forms without writing JSON
- **FR-022**: Visual form builder MUST support common field types (text input, number, date, dropdown, radio buttons, checkboxes, file upload, textarea)
- **FR-023**: Visual form builder MUST allow configuration of field validation rules (required, min/max length, regex patterns, custom validation)
- **FR-024**: System MUST generate valid JSON schema from visual form builder designs and allow manual JSON editing by developers
- **FR-025**: Visual form builder MUST provide live preview of forms as they are being designed

#### RBAC and Identity Management

- **FR-026**: System MUST support multi-tenant architecture where each tenant's users, roles, and data are completely isolated
- **FR-027**: System MUST support hierarchical department/group structures for task assignment and approval routing
- **FR-028**: System MUST synchronize user, role, and department data to the workflow engine for expression-based task assignment (e.g., `${deptManager}`)
- **FR-029**: System MUST enforce role-based access controls for all administrative operations (process deployment, instance management, user management)
- **FR-030**: System MUST prevent cross-tenant data access at all layers (application, database, cache)
- **FR-031**: System MUST support OAuth2 + OpenID Connect authentication with optional local username/password fallback, allowing each tenant to configure their preferred authentication provider

#### Collaboration Features

- **FR-032**: System MUST require approval comments for all task completion actions (approve, reject, delegate)
- **FR-033**: System MUST support file attachments at both process and task levels
- **FR-034**: System MUST support @mention functionality in comments to notify specific users
- **FR-035**: System MUST display complete collaboration history (comments and attachments) in process timeline view
- **FR-036**: System MUST send notifications for @mentions, task assignments, and task expirations

#### Audit and Logging

- **FR-037**: System MUST log every workflow action with user ID, tenant ID, timestamp, IP address, and action details
- **FR-038**: System MUST maintain append-only audit logs that cannot be modified or deleted
- **FR-039**: System MUST archive completed process instances and variables to cold storage after a configurable retention period
- **FR-040**: System MUST provide audit log query interface for compliance reporting and forensic analysis
- **FR-041**: System MUST log all administrative interventions (suspend, terminate, variable modification, node jumping)

#### Analytics and Dashboards

- **FR-042**: System MUST provide pre-built dashboards showing task completion efficiency, process instance distribution, and bottleneck analysis
- **FR-043**: System MUST support custom dashboard creation where users can drag and drop chart widgets
- **FR-044**: System MUST calculate and display individual and department-level performance metrics (average task duration, SLA compliance rate)
- **FR-045**: System MUST identify process bottlenecks by calculating average dwell time per node
- **FR-046**: System MUST support chart types including bar charts, line charts, pie charts, and funnel diagrams for metric visualization

#### Administrative Console

- **FR-047**: System MUST support importing standard BPMN, CMMN, and DMN XML files exported from any compliant designer
- **FR-048**: System MUST validate process models during deployment and reject invalid definitions with specific error messages
- **FR-049**: System MUST support multiple versions of the same process definition with a designated "primary version" for new instances
- **FR-050**: System MUST provide in-browser preview of process diagrams and XML source code
- **FR-051**: System MUST allow administrators to suspend and activate running process instances
- **FR-052**: System MUST allow administrators to terminate or delete process instances with proper authorization and audit logging
- **FR-053**: System MUST allow administrators to modify process variables in running instances to resolve data errors
- **FR-054**: System MUST support forced node jumping to move process execution to any arbitrary node
- **FR-055**: System MUST provide user, group, and role management interfaces with create, read, update, and delete operations
- **FR-056**: System MUST provide data dictionary management for form dropdown and radio button enumerations

### Workflow Requirements *(if feature involves Flowable processes)*

- **WFR-001**: Process definitions MUST support all BPMN 2.0, CMMN 1.1, and DMN 1.3 constructs as specified by the standards
- **WFR-002**: Task assignments MUST respect tenant, user, group, and role boundaries with no cross-tenant assignment possible
- **WFR-003**: Process variables MUST be limited to primitives or JSON-serializable objects with maximum 10KB per variable
- **WFR-004**: Form schemas MUST map to process variables using camelCase naming convention for consistency
- **WFR-005**: Audit logging MUST capture userId, tenantId, timestamp, IP address, processInstanceId, and actionType for all operations
- **WFR-006**: Process instances MUST maintain complete execution history including node visits, variable changes, and task assignments
- **WFR-007**: Task expiration MUST be evaluated based on business calendar excluding non-working hours and holidays
- **WFR-008**: Process suspension MUST stop all timer events, message events, and job execution for the affected instance
- **WFR-009**: Form field permissions MUST be evaluated server-side before rendering and validated on form submission
- **WFR-010**: Process variable modifications by administrators MUST be logged in the audit trail with before/after values

### Multi-Tenant Requirements *(if feature involves tenant data)*

- **TFR-001**: All database queries MUST filter by tenant_id with no exceptions, including administrative queries
- **TFR-002**: Process instances MUST be isolated by tenant context with cross-tenant instance access prohibited
- **TFR-003**: Cache keys MUST include tenant identifier to prevent cross-tenant cache poisoning
- **TFR-004**: Cross-tenant data access MUST be prohibited at application layer with explicit rejection and audit logging
- **TFR-005**: Tenant deletion MUST cascade to all related data (users, processes, tasks, variables) or be blocked if active instances exist
- **TFR-006**: File uploads and attachments MUST be stored in tenant-isolated containers or paths
- **TFR-007**: Process model deployments MUST be tenant-scoped with tenants unable to access models deployed by other tenants
- **TFR-008**: User authentication MUST validate tenant context and prevent users from accessing other tenants even with valid credentials

### Non-Functional Requirements

#### Observability and Monitoring

- **NFR-001**: System MUST expose Prometheus metrics endpoints for system health monitoring, performance metrics, and resource utilization
- **NFR-002**: System MUST provide pre-built Grafana dashboards for monitoring process execution metrics, task completion rates, system resource usage, and database performance
- **NFR-003**: System MUST support configurable alerting rules for critical conditions (high error rates, long task queues, database connection exhaustion, excessive memory usage)
- **NFR-004**: System MUST provide health check endpoints (/health for liveness, /ready for readiness probes) supporting container orchestration platforms
- **NFR-005**: System MUST log structured application logs with correlation IDs to enable distributed request tracing across service boundaries

### Key Entities *(include if feature involves data)*

- **ProcessDefinition**: Represents a reusable workflow template (BPMN, CMMN, or DMN) with unique key, name, version, and XML content
- **ProcessInstance**: Represents a single execution of a process definition with unique ID, business key, current status, tenant ID, and variable scope
- **Task**: Represents a work item assigned to a user or group with properties including ID, name, assignee, due date, priority, and form reference
- **FormSchema**: Represents a dynamic form definition with JSON structure, field definitions, validation rules, version number, and associated process/node
- **User**: Represents a system user with unique ID, username, email, department assignments, role memberships, and tenant association
- **Role**: Represents a collection of permissions with unique ID, name, description, and granted capabilities
- **Department**: Represents an organizational unit with unique ID, name, parent department (for hierarchy), and member users
- **Tenant**: Represents an isolated organizational boundary with unique ID, name, configuration settings, and associated users/processes
- **AuditLogEntry**: Represents a recorded system action with timestamp, user ID, tenant ID, IP address, action type, entity affected, and details
- **Comment**: Represents user communication attached to a task or process with content, author, timestamp, and mentions
- **Attachment**: Represents a file linked to a task or process with file metadata, uploader, timestamp, and storage reference
- **Dashboard**: Represents a custom analytics view with unique ID, name, owner user, and layout configuration
- **Widget**: Represents a chart component on a dashboard with type, data source, filter parameters, and position

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can start a new process instance and see it appear in "My Requests" within 3 seconds
- **SC-002**: Users can complete a task with form submission in under 60 seconds including approval comment entry
- **SC-003**: System supports 1,000 concurrent process instances without performance degradation beyond 500ms average response time
- **SC-004**: 95% of tasks appear in the assignee's "My Tasks" list within 5 seconds of reaching the task node
- **SC-005**: Users can create and deploy a new process definition (BPMN/CMMN/DMN) in under 2 minutes
- **SC-006**: Historical process instances from 12+ months ago are archived and queryable without affecting performance of active instances
- **SC-007**: Audit logs can be queried and exported for any date range within 30 seconds for compliance reporting
- **SC-008**: Analytics dashboards display data with no more than 5-second lag from actual task completion
- **SC-009**: 90% of users can independently create a custom dashboard with drag-and-drop widgets on their first attempt without training
- **SC-010**: Tenant data isolation is 100% effective with zero cross-tenant data access possible even under administrative override attempts
- **SC-011**: System administrators can resolve 80% of stuck process instances using admin console intervention without database manipulation
- **SC-012**: Process bottlenecks are automatically identified and highlighted in dashboards with accuracy rate of 90%+ compared to manual analysis
- **SC-013**: Form versioning ensures that 100% of historical process instances render with the exact form version active at instance completion time
- **SC-014**: Task delegation and reassignment operations complete within 3 seconds and immediately update task assignee visibility
- **SC-015**: User onboarding time (from account creation to first completed task) averages under 15 minutes with self-guided interface
- **SC-016**: System health metrics are visible in Grafana dashboards within 30 seconds of deployment
- **SC-017**: Critical alerts (system down, database connection lost, error rate > 5%) trigger notifications within 1 minute of threshold violation

## Assumptions

1. **Authentication System**: The platform supports OAuth2 + OpenID Connect for integration with external identity providers (Azure AD, Okta, Google Workspace, etc.) with optional fallback to local username/password authentication for simple deployments. This enables flexible tenant authentication strategies ranging from enterprise SSO to standalone deployments.
2. **Email/Notification Service**: The platform will use standard email protocols for sending notifications. Third-party notification services may be integrated but are not specified in initial scope.
3. **Document Storage**: Attachments and documents will be stored using the project's document storage strategy (local filesystem, cloud storage, or document management system API as referenced in architecture).
4. **Business Calendar**: The system will use a standard business calendar excluding weekends and holidays. Holiday calendars may be tenant-specific but global defaults will apply.
5. **Process Modeling Tool**: The platform does NOT include a built-in process model designer. Users will use external BPMN/CMMN/DMN modeling tools (Flowable Design Cloud, Camunda Modeler, or other compliant designers) to create process definitions, then import the XML files into the platform for deployment and execution.
6. **Database Performance**: The database will be properly configured with connection pooling and indexing to support the specified concurrent user loads.
7. **Multi-Tenant Strategy**: Each tenant will have isolated data at the database row level with tenant_id filtering, rather than separate databases per tenant.
8. **Retention Periods**: Default data retention periods will be 2 years for active data and 7 years for archived audit data unless specific compliance requirements mandate different periods.
9. **User Interface Language**: The primary user interface will be in Chinese with support for internationalization if business requirements expand to other regions.
10. **Browser Support**: The web interface will support modern browsers (Chrome, Firefox, Safari, Edge) released within the last 2 years.
11. **Observability Infrastructure**: Prometheus and Grafana are deployed as separate infrastructure components. The platform exposes metrics endpoints that Prometheus scrapes, and provides Grafana dashboard JSON definitions that operators import into their Grafana instance.
12. **Service Level Agreements**: The platform operates on a best-effort availability basis with no formal uptime SLA commitments. Scheduled maintenance windows and unexpected outages may occur without penalty. This approach is suitable for internal platforms, development/testing environments, or deployments where strict business continuity guarantees are not required.