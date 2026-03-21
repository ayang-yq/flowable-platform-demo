# Quick Testing Guide

**Purpose**: Verify seed data and core functionality
**Prerequisites**: PostgreSQL running, backend compiled

---

## 1. Start Database

```bash
# Start PostgreSQL container
docker-compose up postgres -d

# Wait for database to be ready
docker-compose logs postgres
# Look for "database system is ready to accept connections"
```

## 2. Start Backend Application

```bash
# From backend directory
cd backend
mvn spring-boot:run
```

Wait for: `Started FlowablePlatformApplication in X.XXX seconds`

## 3. Verify Seed Data Loaded

### Test 1: Login with ACME Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "tenantCode": "acme"
  }'
```

**Expected Response:**
```json
{
  "timestamp": "2026-03-21T13:30:00",
  "code": "SUCCESS",
  "message": "Operation successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "423e4567-e89b-12d3-a456-426614174000",
    "username": "admin",
    "tenantCode": "acme",
    "tenantId": "123e4567-e89b-12d3-a456-426614174000"
  }
}
```

### Test 2: Deploy Sample Process

```bash
# Get the token from Test 1
TOKEN="your-token-here"
TENANT_ID="123e4567-e89b-12d3-a456-426614174000"

# Deploy leave request process
curl -X POST http://localhost:8080/api/processes/definitions \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" \
  -F "file=@backend/src/main/resources/processes/samples/leave-request.bpmn20.xml"
```

**Expected Response:**
```json
{
  "code": "SUCCESS",
  "data": {
    "deploymentId": "deployment-123",
    "message": "Process definition deployed successfully"
  }
}
```

### Test 3: Start Process Instance

```bash
curl -X POST http://localhost:8080/api/processes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID" \
  -d '{
    "processDefinitionKey": "leaveRequest",
    "businessKey": "LR-2024-001",
    "variables": {
      "employeeName": "john.doe",
      "managerDepartment": "backend-team"
    }
  }'
```

**Expected Response:**
```json
{
  "code": "SUCCESS",
  "data": {
    "processInstanceId": "proc-123",
    "processDefinitionKey": "leaveRequest",
    "businessKey": "LR-2024-001",
    "suspended": false,
    "variables": {
      "employeeName": "john.doe",
      "managerDepartment": "backend-team"
    }
  }
}
```

### Test 4: List Process Instances

```bash
curl -X GET http://localhost:8080/api/processes \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID"
```

### Test 5: Check Tasks

```bash
curl -X GET http://localhost:8080/api/tasks/my-tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-Id: $TENANT_ID"
```

## 4. Run Seed Data Verification Test

```bash
# From backend directory
cd backend
mvn test -Dtest=SeedDataVerificationTest
```

**Expected Output:** All tests should pass ✅

---

## Troubleshooting

### Issue: "Tenant not found"
**Solution**: Verify seed data migration ran:
```sql
SELECT * FROM tenants WHERE code = 'acme';
```

### Issue: "Invalid credentials"
**Solution**: Check user exists and is active:
```sql
SELECT username, email, is_active FROM users WHERE tenant_id = '123e4567-e89b-12d3-a456-426614174000' AND username = 'admin';
```

### Issue: "Process definition not found"
**Solution**: Deploy process definition first using Test 2

### Issue: Cross-tenant data visible
**Solution**: Verify `X-Tenant-Id` header is being sent with all requests

---

## Quick Test Checklist

- [ ] Database starts successfully
- [ ] Backend application starts
- [ ] Login with admin/admin123/acme works
- [ ] Can deploy BPMN process
- [ ] Can start process instance
- [ ] Can list process instances
- [ ] Can see tasks
- [ ] Multi-tenant isolation works (try demo tenant)

---

## Frontend Testing (Optional)

```bash
# Start frontend
cd frontend
npm install
npm run dev

# Open browser
http://localhost:3000/login
```

Use credentials:
- Username: `admin`
- Password: `admin123`
- Tenant: `acme`

---

**Next Steps**: Once basic tests pass, you can continue with Phase 3 implementation or move to Phase 4 (Task Center).
