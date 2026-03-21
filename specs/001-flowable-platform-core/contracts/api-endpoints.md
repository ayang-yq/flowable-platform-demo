# REST API Contracts

**Version**: 1.0.0
**Purpose**: Complete REST API endpoint definitions for Flowable Platform Core
**Base URL**: `http://localhost:8080/api` (configurable via environment)

---

## Authentication & Security

### Headers
```
X-Tenant-Id: {tenant-uuid}  # Required for all endpoints except /auth/*
Authorization: Bearer {jwt-token}  # Required after authentication
Content-Type: application/json
Accept: application/json
```

### Response Format (Success)
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation successful",
  "timestamp": "2026-03-21T10:00:00Z"
}
```

### Response Format (Error)
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": { /* additional error context */ }
  },
  "timestamp": "2026-03-21T10:00:00Z"
}
```

### Error Codes
- `AUTHENTICATION_REQUIRED`: No valid JWT token provided
- `TENANT_NOT_FOUND`: X-Tenant-Id header references non-existent tenant
- `TENANT_IS_ACTIVE`: Tenant is not active
- `ACCESS_DENIED`: User lacks required role/permission
- `VALIDATION_ERROR`: Request validation failed
- `RESOURCE_NOT_FOUND`: Requested resource does not exist
- `FLOWABLE_ENGINE_ERROR`: Flowable engine operation failed
- `INTERNAL_SERVER_ERROR`: Unexpected server error

---

## Authentication Endpoints

### POST /auth/login
Local username/password authentication.

**Request**:
```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "uuid",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "roles": ["USER", "PROCESS_DESIGNER"],
      "tenant": {
        "id": "tenant-uuid",
        "name": "Acme Corporation",
        "code": "acme"
      }
    }
  }
}
```

**Errors**: 401 (invalid credentials), 404 (tenant not found)

### POST /auth/oauth2/{provider}
Initiate OAuth2/OIDC authentication with external provider.

**Providers**: `azure`, `google`, `okta` (configurable per tenant)

**Request**: Redirect to provider's authorization endpoint (OAuth2 flow)

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      // User object created/updated from IdP
    }
  }
}
```

### POST /auth/logout
Terminate current session.

**Request**: None (requires Authorization header)

**Response (200)**:
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

### GET /auth/me
Get current authenticated user information.

**Request**: None (requires Authorization header)

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "username": "johndoe",
    "email": "john@example.com",
    "roles": ["USER", "ADMIN"],
    "permissions": ["process:start", "task:complete"],
    "tenant": { /* tenant object */ }
  }
}
```

---

## Process Management Endpoints

### POST /processes
Start a new process instance.

**Request**:
```json
{
  "processDefinitionKey": "leaveRequest",
  "businessKey": "LR-2024-001",
  "variables": {
    "employeeName": "John Doe",
    "leaveDays": 5,
    "startDate": "2024-04-01"
  }
}
```

**Response (201)**:
```json
{
  "success": true,
  "data": {
    "id": "process-instance-uuid",
    "processDefinitionKey": "leaveRequest",
    "businessKey": "LR-2024-001",
    "status": "RUNNING",
    "startTime": "2026-03-21T10:00:00Z",
    "startedBy": "user-uuid",
    "variables": { /* process variables */ }
  }
}
```

**Errors**: 400 (invalid process definition), 403 (permission denied)

### GET /processes
List process instances with pagination and filtering.

**Query Parameters**:
- `page` (default: 0): Page number
- `size` (default: 20): Page size
- `sort` (default: startTime,desc): Sort field and direction
- `status` (optional): Filter by status (RUNNING, SUSPENDED, COMPLETED)
- `processDefinitionKey` (optional): Filter by process definition
- `startedBy` (optional): Filter by user who started instance

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [ /* array of process instances */ ],
    "pageable": {
      "page": 0,
      "size": 20,
      "totalElements": 156,
      "totalPages": 8
    }
  }
}
```

### GET /processes/{id}
Get process instance details.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "process-instance-uuid",
    "processDefinitionKey": "leaveRequest",
    "processDefinitionName": "Leave Request Process",
    "businessKey": "LR-2024-001",
    "status": "RUNNING",
    "startTime": "2026-03-21T10:00:00Z",
    "startedBy": { /* user object */ },
    "currentActivityId": "task-uuid",
    "currentActivityName": "Manager Approval",
    "variables": { /* process variables */ }
  }
}
```

### GET /processes/{id}/diagram
Get process diagram with current node highlighted.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "xml": "<?xml version=\"1.0\"...>  <!-- BPMN XML -->",
    "svg": "<svg>...</svg>",  // Rendered SVG with highlighted current node
    "currentNode": "ManagerApproval",
    "completedNodes": ["StartEvent", "SubmitLeave"],
    "pendingNodes": ["ManagerApproval", "HRNotification"]
  }
}
```

### POST /processes/{id}/suspend
Suspend a running process instance.

**Request**:
```json
{
  "reason": "Pending investigation"
}
```

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "process-instance-uuid",
    "status": "SUSPENDED"
  }
}
```

### POST /processes/{id}/activate
Activate a suspended process instance.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "process-instance-uuid",
    "status": "RUNNING"
  }
}
```

### POST /processes/{id}/terminate
Terminate (forcefully stop) a process instance.

**Request**:
```json
{
  "reason": "Process error - cannot proceed"
}
```

**Response (200)**:
```json
{
  "success": true,
  "message": "Process instance terminated"
}
```

### GET /processes/definitions
List deployed process definitions.

**Query Parameters**: page, size, sort

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "key": "leaveRequest",
        "name": "Leave Request Process",
        "version": "1.0.0",
        "deploymentId": "deployment-uuid",
        "isPrimary": true,
        "suspended": false
      }
    ]
  }
}
```

### POST /processes/definitions
Deploy a new process definition (XML file upload).

**Request**: multipart/form-data with file field

**Response (201)**:
```json
{
  "success": true,
  "data": {
    "deploymentId": "deployment-uuid",
    "processDefinitions": [
      {
        "key": "leaveRequest",
        "name": "Leave Request Process",
        "version": "1.0.0"
      }
    ]
  }
}
```

### GET /processes/definitions/{key}/xml
Get process definition XML source.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "xml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
  }
}
```

---

## Task Management Endpoints

### GET /tasks/my-tasks
Get current user's pending tasks.

**Query Parameters**: page, size, sort, priority (optional)

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "task-uuid",
        "name": "Manager Approval",
        "description": "Review and approve leave request",
        "assignee": "user-uuid",
        "priority": 50,
        "dueDate": "2026-03-25T10:00:00Z",
        "createTime": "2026-03-21T10:00:00Z",
        "processInstance": { /* process instance summary */ },
        "formSchema": { /* task form schema */ }
      }
    ],
    "pageable": { /* pagination info */ }
  }
}
```

### GET /tasks/completed
Get current user's completed tasks.

**Query Parameters**: page, size, sort

**Response (200)**: Similar structure to my-tasks

### GET /tasks/my-requests
Get process instances started by current user.

**Query Parameters**: page, size, sort, status

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [ /* process instances */ ]
  }
}
```

### GET /tasks/{id}
Get task details.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "name": "Manager Approval",
    "assignee": { /* assignee user object */ },
    "candidateUsers": [ /* array of candidate users */ ],
    "candidateGroups": [ /* array of candidate groups */ ],
    "priority": 50,
    "dueDate": "2026-03-25T10:00:00Z",
    "formSchema": { /* form schema for task */ },
    "processInstance": { /* process instance */ },
    "comments": [ /* task comments */ ],
    "attachments": [ /* task attachments */ ]
  }
}
```

### POST /tasks/{id}/claim
Claim a task (set assignee to current user).

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "assignee": "current-user-uuid"
  }
}
```

### POST /tasks/{id}/complete
Complete a task with form data.

**Request**:
```json
{
  "formData": {
    "approved": true,
    "comments": "Leave approved - enjoy your time off!"
  },
  "variables": {
    "approvedBy": "johndoe",
    "approvedAt": "2026-03-21T11:00:00Z"
  }
}
```

**Response (200)**:
```json
{
  "success": true,
  "message": "Task completed successfully"
}
```

### POST /tasks/{id}/delegate
Delegate task to another user.

**Request**:
```json
{
  "delegateTo": "user-uuid",
  "reason": "Out of office - delegating to backup"
}
```

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "task-uuid",
    "assignee": "delegate-user-uuid"
  }
}
```

---

## Form Management Endpoints

### GET /forms
List form schemas.

**Query Parameters**: page, size, processDefinitionKey (optional)

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "form-schema-uuid",
        "name": "Leave Request Form",
        "version": "1.0.0",
        "processDefinitionKey": "leaveRequest",
        "taskDefinitionKey": "submitLeave",
        "isActive": true
      }
    ]
  }
}
```

### GET /forms/{id}
Get form schema details.

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "id": "form-schema-uuid",
    "name": "Leave Request Form",
    "version": "1.0.0",
    "schema": { /* SurveyJS schema JSON */ },
    "processVariableMapping": { /* variable mappings */ }
  }
}
```

### POST /forms
Create new form schema (from visual builder).

**Request**:
```json
{
  "name": "Expense Report Form",
  "processDefinitionKey": "expenseReport",
  "taskDefinitionKey": "submitExpense",
  "schema": { /* SurveyJS schema JSON */ },
  "processVariableMapping": { /* variable mappings */ }
}
```

**Response (201)**:
```json
{
  "success": true,
  "data": {
    "id": "form-schema-uuid",
    "version": "1.0.0"
  }
}
```

---

## User & RBAC Endpoints

### GET /users
List users (paginated).

**Query Parameters**: page, size, departmentId (optional), roleId (optional)

### POST /users
Create new user.

**Request**:
```json
{
  "username": "janedoe",
  "email": "jane@example.com",
  "password": "securePassword123",
  "firstName": "Jane",
  "lastName": "Doe",
  "roleIds": ["role-uuid"],
  "departmentIds": ["dept-uuid"]
}
```

### GET /roles
List all roles.

### POST /roles
Create new role.

**Request**:
```json
{
  "name": "Reviewer",
  "code": "REVIEWER",
  "permissions": ["task:complete", "task:claim"]
}
```

---

## Admin Console Endpoints

### GET /admin/instances
List all process instances (admin view, all tenants).

**Response (200)**:
```json
{
  "success": true,
  "data": {
    "content": [ /* all process instances across all tenants */ ]
  }
}
```

### POST /admin/instances/{id}/variables
Modify process instance variables (admin intervention).

**Request**:
```json
{
  "variableName": "approvalCount",
  "variableValue": 3,
  "reason": "Correct loop count from data error"
}
```

### POST /admin/instances/{id}/jump
Force jump process instance to specific node.

**Request**:
```json
{
  "targetNodeId": "AdminApproval",
  "reason": "Skip intermediate approval due to escalation"
}
```

---

## Health & Monitoring Endpoints

### GET /actuator/health
Liveness/readiness probe for container orchestration.

**Response (200)**:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "flowable": { "status": "UP" }
  }
}
```

### GET /actuator/metrics
Prometheus metrics endpoint for observability.

**Response**: Plain text metrics format

---

**API contracts complete and ready for frontend integration**.
