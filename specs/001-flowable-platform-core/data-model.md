# Data Model: Flowable Platform Core

**Version**: 1.0.0
**Date**: 2026-03-21
**Purpose**: Complete entity definitions, relationships, and validation rules for multi-tenant Flowable platform

---

## Entity Relationship Diagram

```
Tenant (1) ----< (*) User
    |                  |
    |                  +----< (*) Role
    |                  |
    |                  +----< (*) Department
    |
    +----< (*) FormSchema
    |
    +----< (*) ProcessInstance (Flowable)
    |
    +----< (*) AuditLog
    |
    +----< (*) Comment
    |
    +----< (*) Attachment
    |
    +----< (*) Dashboard
    |
    +----< (*) Widget

Department (1) ----< (*) User  (self-reference for hierarchy)

FormSchema (1) ----> ProcessInstance (N)  (logical reference through process definition)
ProcessInstance (1) ----> Task (N)
Task (1) ----> Comment (N)
Task (1) ----> Attachment (N)
```

---

## Core Entities

### 1. Tenant

**Purpose**: Isolated organizational boundary with complete data separation

**Table Name**: `tenants`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| name | VARCHAR(255) | NOT NULL | Display name |
| code | VARCHAR(50) | NOT NULL, UNIQUE | Short code for API references |
| domain | VARCHAR(255) | UNIQUE | Custom domain for tenant (optional) |
| logo_url | VARCHAR(500) | NULL | Company logo URL |
| settings | JSONB | NULL | Tenant-specific settings (JSON) |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Active status for soft delete |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_tenant_code` ON (code)
- `idx_tenant_active` ON (is_active)

**Relationships**:
- One-to-many with User
- One-to-many with Role
- One-to-many with Department
- One-to-many with FormSchema
- One-to-many with AuditLog
- One-to-many with Dashboard

**Validation Rules**:
- `code`: Must be alphanumeric with underscores, max 50 chars
- `domain`: Must be valid domain format if provided
- `settings`: Must be valid JSON if provided

**State Transitions**: None (only soft delete via is_active)

---

### 2. User

**Purpose**: System user accounts with multi-tenant isolation

**Table Name**: `users`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| username | VARCHAR(100) | NOT NULL | Login username |
| email | VARCHAR(255) | NOT NULL | Email address |
| password | VARCHAR(255) | NOT NULL | Hashed password (bcrypt) |
| first_name | VARCHAR(100) | NULL | First name |
| last_name | VARCHAR(100) | NULL | Last name |
| display_name | VARCHAR(255) | NULL | Computed display name |
| avatar_url | VARCHAR(500) | NULL | Profile picture URL |
| phone | VARCHAR(20) | NULL | Phone number |
| locale | VARCHAR(10) | NULL, DEFAULT 'en' | Language preference |
| timezone | VARCHAR(50) | NULL, DEFAULT 'UTC' | Timezone preference |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Active status |
| email_verified | BOOLEAN | NOT NULL, DEFAULT FALSE | Email verification status |
| last_login_at | TIMESTAMP | NULL | Last successful login |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_user_tenant_username` ON (tenant_id, username) UNIQUE
- `idx_user_tenant_email` ON (tenant_id, email) UNIQUE
- `idx_user_tenant_active` ON (tenant_id, is_active)

**Relationships**:
- Many-to-one: Tenant
- Many-to-many: Role (through user_roles join table)
- Many-to-many: Department (through user_departments join table)
- One-to-many: AuditLog (as actor)
- One-to-many: Comment (as author)
- One-to-many: Attachment (as uploader)
- One-to-many: Dashboard (as owner)

**Validation Rules**:
- `username`: 3-50 chars, alphanumeric with dots, hyphens, underscores
- `email`: Valid email format per RFC 5322
- `password`: Must be bcrypt hashed, never stored in plain text
- `phone`: E.164 format if provided

**Unique Constraints**:
- (tenant_id, username) combination must be unique
- (tenant_id, email) combination must be unique

---

### 3. Role

**Purpose**: Collection of permissions for access control

**Table Name**: `roles`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| name | VARCHAR(100) | NOT NULL | Display name |
| code | VARCHAR(50) | NOT NULL | Short code for API references |
| description | TEXT | NULL | Role description |
| permissions | JSONB | NULL | Permission definitions (JSON) |
| is_system | BOOLEAN | NOT NULL, DEFAULT FALSE | System role (cannot be deleted) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_role_tenant_code` ON (tenant_id, code) UNIQUE

**Relationships**:
- Many-to-one: Tenant
- Many-to-many: User (through user_roles join table)

**Predefined System Roles** (is_system = true):
- `SUPER_ADMIN`: Full system access
- `ADMIN`: Administrative access within tenant
- `PROCESS_DESIGNER`: Can deploy process definitions
- `USER`: Standard user access

**Validation Rules**:
- `code`: Alphanumeric with underscores, max 50 chars, unique per tenant

---

### 4. Department

**Purpose**: Organizational hierarchy for task assignment and approval routing

**Table Name**: `departments`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| parent_id | UUID | NULL, FOREIGN KEY → departments(id) | Parent department (hierarchy) |
| name | VARCHAR(255) | NOT NULL | Department name |
| code | VARCHAR(50) | NOT NULL | Short code for API references |
| manager_id | UUID | NULL, FOREIGN KEY → users(id) | Department manager |
| path | VARCHAR(1000) | NULL | Materialized path for hierarchy queries |
| level | INTEGER | NOT NULL, DEFAULT 0 | Hierarchy depth |
| is_active | BOOLEAN | NOT NULL, DEFAULT TRUE | Active status |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_dept_tenant_code` ON (tenant_id, code) UNIQUE
- `idx_dept_parent` ON (parent_id)
- `idx_dept_path` ON (path)  // For hierarchy queries

**Relationships**:
- Many-to-one: Tenant
- Self-reference: parent_id → departments(id)
- One-to-many: Department (parent-child)
- Many-to-many: User (through user_departments join table)
- Many-to-one: User (as manager)

**Hierarchy Example**:
```
Engineering (level 1, path = /engineering/)
  ├─ Backend Team (level 2, path = /engineering/backend/)
  └─ Frontend Team (level 2, path = /engineering/frontend/)
```

**Validation Rules**:
- `code`: Alphanumeric with underscores, max 50 chars, unique per tenant
- `parent_id`: Cannot reference self (no circular references)
- `level`: Automatically calculated based on parent level

---

### 5. FormSchema

**Purpose**: Dynamic form definitions with version control

**Table Name**: `form_schemas`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| name | VARCHAR(255) | NOT NULL | Form name |
| description | TEXT | NULL | Form description |
| version | VARCHAR(20) | NOT NULL | Semantic version (1.0.0) |
| process_definition_key | VARCHAR(255) | NULL | Associated process definition |
| task_definition_key | VARCHAR(255) | NULL | Associated task node (NULL for global forms) |
| schema_json | JSONB | NOT NULL | SurveyJS form schema |
| process_variable_mapping | JSONB | NULL | Form field to process variable mapping |
| is_active | BOOLEAN | NOT NULL, DEFAULT FALSE | Active status |
| created_by | UUID | FOREIGN KEY → users(id) | Creator |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_form_tenant_process_version` ON (tenant_id, process_definition_key, version)
- `idx_form_tenant_active` ON (tenant_id, is_active)

**Relationships**:
- Many-to-one: Tenant
- Many-to-one: User (as creator)

**Validation Rules**:
- `version`: Must follow semantic versioning (major.minor.patch)
- `schema_json`: Must be valid SurveyJS schema JSON
- `process_variable_mapping`: Must be valid JSON object if provided

**Version Control**:
- New versions create new FormSchema records
- Historical process instances reference the exact form version active at completion time
- Only one version per (process_definition_key, version) combination can be is_active=true

---

### 6. AuditLog

**Purpose**: Immutable audit trail of all system actions

**Table Name**: `audit_logs`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| user_id | UUID | NULL, FOREIGN KEY → users(id) | User who performed action |
| username | VARCHAR(100) | NOT NULL | Username snapshot |
| timestamp | TIMESTAMP | NOT NULL, DEFAULT NOW() | Action timestamp |
| ip_address | VARCHAR(45) | NULL | Client IP address (IPv6 compatible) |
| action_type | VARCHAR(50) | NOT NULL | Action category (e.g., PROCESS_STARTED, TASK_COMPLETED) |
| entity_type | VARCHAR(50) | NULL | Entity type (e.g., PROCESS_INSTANCE, TASK) |
| entity_id | VARCHAR(255) | NULL | Entity identifier |
| details | JSONB | NULL | Additional action details (JSON) |
| session_id | VARCHAR(255) | NULL | Session identifier |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Log creation timestamp |

**Indexes**:
- `idx_audit_tenant_user` ON (tenant_id, user_id)
- `idx_audit_timestamp` ON (timestamp DESC)
- `idx_audit_action` ON (action_type)
- `idx_audit_entity` ON (entity_type, entity_id)

**Relationships**:
- Many-to-one: Tenant
- Many-to-one: User (optional)

**Action Type Categories**:
- `PROCESS_STARTED`: Process instance creation
- `PROCESS_COMPLETED`: Process instance finished
- `PROCESS_SUSPENDED`: Process instance suspended
- `TASK_CREATED`: Task created
- `TASK_ASSIGNED`: Task assigned to user
- `TASK_CLAIMED`: Task claimed by user
- `TASK_COMPLETED`: Task completed
- `TASK_DELEGATED`: Task delegated to another user
- `FORM_SUBMITTED`: Form data submitted
- `USER_LOGIN`: User authentication
- `USER_LOGOUT`: User logout
- `ADMIN_INTERVENTION`: Admin action (suspend, terminate, variable modification)

**Validation Rules**:
- `timestamp`: Must be valid timestamp
- `ip_address`: Valid IPv4 or IPv6 address if provided
- `details`: Must be valid JSON if provided

**Append-Only Policy**:
- Once created, AuditLog records CANNOT be modified or deleted
- Updates only allowed through new records (correction entries)

---

### 7. Comment

**Purpose**: Collaboration comments on tasks and process instances

**Table Name**: `comments`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| author_id | UUID | NOT NULL, FOREIGN KEY → users(id) | Comment author |
| content | TEXT | NOT NULL | Comment text |
| task_id | VARCHAR(255) | NULL | Task identifier (Flowable) |
| process_instance_id | VARCHAR(255) | NULL | Process instance identifier (Flowable) |
| mentions | JSONB | NULL | Array of mentioned user IDs |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last edit timestamp |

**Indexes**:
- `idx_comment_task` ON (task_id)
- `idx_comment_instance` ON (process_instance_id)
- `idx_comment_author` ON (author_id)

**Relationships**:
- Many-to-one: Tenant
- Many-to-one: User (as author)

**Validation Rules**:
- `content`: 1-10000 characters
- Either `task_id` OR `process_instance_id` must be specified (not both)
- `mentions`: Array of UUIDs if provided

**Mention Support**:
- Mentions stored as JSON array of user IDs: `["uuid1", "uuid2"]`
- Notification triggered for each mentioned user

---

### 8. Attachment

**Purpose**: File attachments for tasks and process instances

**Table Name**: `attachments`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| file_name | VARCHAR(255) | NOT NULL | Original file name |
| file_size | BIGINT | NOT NULL | File size in bytes |
| mime_type | VARCHAR(100) | NOT NULL | MIME type (e.g., application/pdf) |
| storage_path | VARCHAR(500) | NOT NULL | File storage path |
| storage_provider | VARCHAR(50) | NOT NULL | Storage system (local, s3, azure) |
| uploader_id | UUID | NOT NULL, FOREIGN KEY → users(id) | Uploader |
| task_id | VARCHAR(255) | NULL | Associated task identifier |
| process_instance_id | VARCHAR(255) | NULL | Associated process instance identifier |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Upload timestamp |

**Indexes**:
- `idx_attachment_task` ON (task_id)
- `idx_attachment_instance` ON (process_instance_id)
- `idx_attachment_uploader` ON (uploader_id)

**Relationships**:
- Many-to-one: Tenant
- Many-to-one: User (as uploader)

**Storage Strategy**:
- Files stored with tenant isolation: `/tenant/{tenantId}/attachments/{id}/{filename}`
- Actual storage in external system (document management API) per architecture.md

**Validation Rules**:
- `file_name`: 1-255 characters
- `file_size`: Must be > 0
- `mime_type`: Valid MIME type format
- Either `task_id` OR `process_instance_id` must be specified

---

### 9. Dashboard

**Purpose**: User-customizable analytics dashboards

**Table Name**: `dashboards`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| owner_id | UUID | NOT NULL, FOREIGN KEY → users(id) | Dashboard owner |
| name | VARCHAR(255) | NOT NULL | Dashboard name |
| description | TEXT | NULL | Dashboard description |
| layout_json | JSONB | NOT NULL | Dashboard layout configuration |
| is_public | BOOLEAN | NOT NULL, DEFAULT FALSE | Shared with others in tenant |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_dashboard_tenant_owner` ON (tenant_id, owner_id)

**Relationships**:
- Many-to-one: Tenant
- Many-to-one: User (as owner)
- One-to-many: Widget

**Layout JSON Structure**:
```json
{
  "version": "1.0",
  "widgets": [
    {
      "id": "widget-1",
      "type": "task-efficiency",
      "position": {"x": 0, "y": 0, "w": 4, "h": 3},
      "title": "My Task Efficiency",
      "config": {"timeRange": "30d"}
    }
  ]
}
```

**Validation Rules**:
- `name`: 1-255 characters, unique per owner
- `layout_json`: Must be valid JSON matching layout schema

---

### 10. Widget

**Purpose**: Individual chart/metric components on dashboards

**Table Name**: `widgets`

**Fields**:
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| dashboard_id | UUID | NOT NULL, FOREIGN KEY → dashboards(id) | Parent dashboard |
| tenant_id | UUID | NOT NULL, FOREIGN KEY → tenants(id) | Tenant association |
| type | VARCHAR(50) | NOT NULL | Widget type |
| title | VARCHAR(255) | NOT NULL | Widget title |
| data_source | VARCHAR(100) | NOT NULL | Data source identifier |
| filter_config | JSONB | NULL | Filter parameters (JSON) |
| position | INTEGER | NOT NULL | Position in dashboard (0-based) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | Creation timestamp |

**Indexes**:
- `idx_widget_dashboard` ON (dashboard_id)
- `idx_widget_position` ON (dashboard_id, position)

**Relationships**:
- Many-to-one: Dashboard
- Many-to-one: Tenant

**Widget Types**:
- `task-efficiency`: Task completion efficiency metrics
- `process-distribution`: Process instance distribution chart
- `bottleneck-analysis`: Process bottleneck identification
- `sla-compliance`: SLA breach rate tracking

**Validation Rules**:
- `type`: Must be valid widget type
- `position`: Integer 0-19 (max 20 widgets per dashboard)
- `filter_config`: Must be valid JSON if provided

---

## Join Tables

### user_roles (Many-to-Many: User ↔ Role)

| Field | Type | Constraints |
|-------|------|-------------|
| user_id | UUID | NOT NULL, FK → users(id) |
| role_id | UUID | NOT NULL, FK → roles(id) |
| assigned_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |

**Primary Key**: (user_id, role_id)

### user_departments (Many-to-Many: User ↔ Department)

| Field | Type | Constraints |
|-------|------|-------------|
| user_id | UUID | NOT NULL, FK → users(id) |
| department_id | UUID | NOT NULL, FK → departments(id) |
| is_manager | BOOLEAN | NOT NULL, DEFAULT FALSE |
| joined_at | TIMESTAMP | NOT NULL, DEFAULT NOW() |

**Primary Key**: (user_id, department_id)

---

## State Transition Diagrams

### ProcessInstance States

```
[CREATED] → [RUNNING] → [SUSPENDED] → [ACTIVATED] → [RUNNING] → [COMPLETED]
              ↓
         [TERMINATED]
```

### Task States

```
[CREATED] → [ASSIGNED] → [CLAIMED] → [COMPLETED]
              ↓           ↓
         [DELEGATED]  [CANCELLED]
```

### User States

```
[ACTIVE] → [INACTIVE]
```

### FormSchema States

```
[ACTIVE] → [SUPERSEDED] (when new version created)
```

---

## Database Constraints Summary

**Multi-Tenant Isolation**:
- All tables include `tenant_id` FK (except tenancy metadata tables)
- All queries must filter by `tenant_id`
- Unique constraints include `tenant_id` in composite keys

**Data Integrity**:
- Foreign key constraints ensure referential integrity
- ON DELETE RESTRICT for most relationships (prevent orphaned data)
- ON DELETE SET NULL for soft delete scenarios

**Performance Optimization**:
- Composite indexes on (tenant_id, frequently_filtered_column)
- Indexes on all foreign key columns
- Partial indexes for common queries (e.g., is_active = true)

**Audit Trail**:
- AuditLog table is append-only (no UPDATE/DELETE operations)
- All user actions logged before execution
- Immutable records ensure compliance and forensic capability

---

## Migration Strategy

1. **Phase 1**: Create core tables (Tenant, User, Role, Department)
2. **Phase 2**: Create workflow tables (FormSchema, AuditLog)
3. **Phase 3**: Create collaboration tables (Comment, Attachment)
4. **Phase 4**: Create analytics tables (Dashboard, Widget)
5. **Phase 5**: Create join tables and constraints
6. **Phase 6**: Create indexes for performance
7. **Phase 7**: Insert seed data (system roles, admin user)

**Flyway Integration**:
- Use `db/migration/V1__create_core_tables.sql`
- Use `db/migration/V2__create_workflow_tables.sql`
- Use `db/migration/V3__create_collaboration_tables.sql`
- Use `db/migration/V4__create_analytics_tables.sql`
- Use `db/migration/V5__create_indexes.sql`
- Use `db/migration/V6__insert_seed_data.sql`

---

**Data model complete and ready for implementation**.
