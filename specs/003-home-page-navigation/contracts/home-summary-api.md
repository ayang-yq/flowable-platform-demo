# API Contract: Home Summary

**Endpoint**: `GET /api/home/summary`
**Authentication**: Required (Bearer JWT)
**Tenant**: Required (X-Tenant-Id header)

## Request

No request body or query parameters.

## Response (200 OK)

```json
{
  "timestamp": "2026-03-22T10:30:00",
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "pendingTaskCount": 5,
    "activeProcessCount": 3,
    "recentActivity": [
      {
        "id": "task-123",
        "type": "TASK_COMPLETED",
        "title": "Review Leave Request",
        "timestamp": "2026-03-22T09:15:00",
        "processDefinitionKey": "leave-request"
      },
      {
        "id": "proc-456",
        "type": "PROCESS_STARTED",
        "title": "Expense Report",
        "timestamp": "2026-03-22T08:30:00",
        "processDefinitionKey": "expense-report"
      }
    ]
  }
}
```

## Error Responses

| Status | Code | Scenario |
|--------|------|----------|
| 401 | UNAUTHORIZED | Missing or invalid JWT |
| 500 | INTERNAL_ERROR | Flowable service unavailable |

## Implementation Notes

- `pendingTaskCount`: Query `TaskService.createTaskQuery().taskAssignee(username).count()`
- `activeProcessCount`: Query `RuntimeService.createProcessInstanceQuery().startedBy(username).count()`
- `recentActivity`: Query `HistoryService` for last 5 completed tasks + started processes, sorted by timestamp desc
- All queries scoped to current tenant via existing `MultiTenantFilter`
