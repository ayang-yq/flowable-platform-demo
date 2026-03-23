# API Contract: Workspace Instance Management

**Branch**: `004-workspace-instance-dashboard` | **Date**: 2026-03-23
**Base Path**: `/api/workspace`

## Endpoints

### 1. List Available Definitions

**GET** `/api/workspace/definitions`

Lists all BPMN process definitions, CMMN case definitions, and DMN decision definitions available for the current tenant.

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| type | String | No | all | Filter by type: `BPMN`, `CMMN`, `DMN`, or `all` |

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:00:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": [
    {
      "id": "simpleApproval:1:12345",
      "key": "simpleApproval",
      "name": "Simple Approval Process",
      "version": 1,
      "category": "approval",
      "type": "BPMN",
      "hasStartForm": true,
      "deploymentTime": "2026-03-20T08:00:00"
    },
    {
      "id": "simpleCase:1:67890",
      "key": "simpleCase",
      "name": "Simple Case",
      "version": 1,
      "category": null,
      "type": "CMMN",
      "hasStartForm": false,
      "deploymentTime": "2026-03-20T08:00:00"
    },
    {
      "id": "simpleDecision:1:11111",
      "key": "simpleDecision",
      "name": "Simple Decision",
      "version": 1,
      "category": null,
      "type": "DMN",
      "hasStartForm": false,
      "deploymentTime": "2026-03-20T08:00:00"
    }
  ]
}
```

---

### 2. Start BPMN Process Instance

**POST** `/api/workspace/processes/{definitionKey}/start`

**Request Body**:
```json
{
  "variables": {
    "applicantName": "John Doe",
    "amount": 5000
  },
  "businessKey": "REQ-2026-001"
}
```

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:01:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "id": "proc-inst-abc123",
    "definitionId": "simpleApproval:1:12345",
    "definitionKey": "simpleApproval",
    "definitionName": "Simple Approval Process",
    "type": "BPMN",
    "startTime": "2026-03-23T10:01:00",
    "endTime": null,
    "duration": null,
    "startedBy": "admin",
    "status": "ACTIVE",
    "businessKey": "REQ-2026-001",
    "tenantId": "tenant-1"
  }
}
```

**Error** `404`:
```json
{
  "code": "NOT_FOUND",
  "message": "Process definition not found: unknownKey"
}
```

---

### 3. Start CMMN Case Instance

**POST** `/api/workspace/cases/{definitionKey}/start`

**Request Body**:
```json
{
  "variables": {
    "caseSubject": "Review request"
  },
  "businessKey": "CASE-2026-001"
}
```

**Response** `200 OK`: Same InstanceDTO shape as process start, with `"type": "CMMN"`.

---

### 4. Execute DMN Decision

**POST** `/api/workspace/decisions/{definitionKey}/execute`

**Request Body**:
```json
{
  "inputVariables": {
    "age": 25,
    "income": 50000
  }
}
```

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:02:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "decisionKey": "simpleDecision",
    "decisionName": "Simple Decision",
    "executionId": "dmn-exec-xyz789",
    "inputVariables": {
      "age": 25,
      "income": 50000
    },
    "outputVariables": [
      {
        "approval": true
      }
    ],
    "executionTime": "2026-03-23T10:02:00"
  }
}
```

---

### 5. List Active Instances (Paginated)

**GET** `/api/workspace/instances/active`

**Query Parameters**:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| page | Integer | No | 0 | Page number (0-based) |
| size | Integer | No | 20 | Page size |
| type | String | No | all | Filter: `BPMN`, `CMMN`, or `all` |
| search | String | No | null | Search by instance name or business key |
| startedBy | String | No | null | Filter by starter (for "My Instances" toggle) |
| startDateFrom | String | No | null | ISO date filter lower bound |
| startDateTo | String | No | null | ISO date filter upper bound |

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:03:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": "proc-inst-abc123",
        "definitionId": "simpleApproval:1:12345",
        "definitionKey": "simpleApproval",
        "definitionName": "Simple Approval Process",
        "type": "BPMN",
        "startTime": "2026-03-23T10:01:00",
        "endTime": null,
        "duration": null,
        "startedBy": "admin",
        "status": "ACTIVE",
        "businessKey": "REQ-2026-001",
        "tenantId": "tenant-1"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
}
```

---

### 6. List Completed Instances (Paginated)

**GET** `/api/workspace/instances/completed`

**Query Parameters**: Same as active instances, plus:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| status | String | No | all | Filter: `COMPLETED`, `CANCELLED`, `FAILED`, or `all` |
| endDateFrom | String | No | null | ISO date filter lower bound |
| endDateTo | String | No | null | ISO date filter upper bound |

**Response** `200 OK`: Same InstancePageDTO shape. `endTime` and `duration` fields are populated.

---

### 7. Get Instance Detail

**GET** `/api/workspace/instances/{id}`

**Query Parameters**:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| type | String | Yes | `BPMN`, `CMMN`, or `DMN` |

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:04:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "id": "proc-inst-abc123",
    "definitionId": "simpleApproval:1:12345",
    "definitionKey": "simpleApproval",
    "definitionName": "Simple Approval Process",
    "type": "BPMN",
    "startTime": "2026-03-23T10:01:00",
    "endTime": null,
    "duration": null,
    "startedBy": "admin",
    "status": "ACTIVE",
    "businessKey": "REQ-2026-001",
    "tenantId": "tenant-1",
    "variables": {
      "applicantName": "John Doe",
      "amount": 5000
    },
    "currentActivities": ["Review Application"],
    "tasks": [
      {
        "id": "task-001",
        "name": "Review Application",
        "assignee": "reviewer1",
        "createTime": "2026-03-23T10:01:05"
      }
    ]
  }
}
```

---

### 8. Get Dashboard Summary

**GET** `/api/workspace/summary`

**Response** `200 OK`:
```json
{
  "timestamp": "2026-03-23T10:05:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "activeCount": 42,
    "completedCount": 156,
    "startedTodayCount": 7,
    "myActiveCount": 3
  }
}
```

---

## Authentication & Headers

All endpoints require:

| Header | Required | Description |
|--------|----------|-------------|
| Authorization | Yes | `Bearer {jwt-token}` |
| X-Tenant-Id | Yes | Current tenant identifier |

## Error Responses

All errors follow the `ApiResponse` format:

| Status | Code | When |
|--------|------|------|
| 400 | VALIDATION_ERROR | Invalid input, missing required fields |
| 401 | UNAUTHORIZED | Missing/invalid JWT token |
| 403 | FORBIDDEN | Insufficient permissions |
| 404 | NOT_FOUND | Definition or instance not found |
| 500 | INTERNAL_ERROR | Unexpected server error |
