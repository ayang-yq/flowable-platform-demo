# API Contract: Login Response (Enhanced)

**Endpoint**: `POST /api/auth/login`
**Status**: Existing endpoint — response needs additional fields

## Current Response Fields (keep)

- `token`, `userId`, `username`, `tenantCode`, `tenantId`

## New Response Fields (add)

| Field | Type | Description |
|-------|------|-------------|
| displayName | String | User's display name (nullable, falls back to username on client) |
| roles | List\<String\> | List of role names assigned to the user |

## Enhanced Response (200 OK)

```json
{
  "timestamp": "2026-03-22T10:30:00",
  "code": "SUCCESS",
  "message": "Success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "username": "admin",
    "displayName": "John Admin",
    "tenantCode": "acme",
    "tenantId": "660e8400-e29b-41d4-a716-446655440001",
    "roles": ["ADMIN", "USER"]
  }
}
```

## Implementation Notes

- Fetch `user.getDisplayName()` from User entity (already loaded during auth)
- Fetch `user.getRoles()` and map to list of `role.getName()`
- Add `displayName` and `roles` fields to `LoginResponse` DTO
- Frontend stores these in localStorage alongside existing auth data
