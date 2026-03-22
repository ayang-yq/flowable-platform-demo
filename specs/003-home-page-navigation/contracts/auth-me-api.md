# API Contract: Auth Me (Enhanced)

**Endpoint**: `GET /api/auth/me`
**Authentication**: Required (Bearer JWT)
**Status**: Existing endpoint — needs enhancement to return full user info

## Request

No request body or query parameters.

## Response (200 OK)

```json
{
  "timestamp": "2026-03-22T10:30:00",
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "displayName": "John Admin",
    "email": "admin@acme.com",
    "tenantCode": "acme",
    "tenantId": "660e8400-e29b-41d4-a716-446655440001",
    "roles": ["ADMIN", "USER"],
    "avatarUrl": null
  }
}
```

## Error Responses

| Status | Code | Scenario |
|--------|------|----------|
| 401 | UNAUTHORIZED | Missing or invalid JWT |

## Implementation Notes

- Extract username from JWT token via `SecurityContextHolder`
- Look up User entity with roles eager-fetched
- Map Role entities to list of role name strings
- Use `displayName` field, falling back to `username` if null
