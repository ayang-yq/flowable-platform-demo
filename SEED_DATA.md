# Seed Data Documentation

**Purpose**: Initial data setup for testing and development
**Date**: 2026-03-21
**Version**: 1.0.0

---

## Default Credentials

### ACME Tenant (Production-like)
- **Tenant Code**: `acme`
- **Admin Username**: `admin`
- **Admin Password**: `admin123`
- **Admin Email**: `admin@acme.com`

### Demo Tenant (Testing)
- **Tenant Code**: `demo`
- **Admin Username**: `demo_admin`
- **Admin Password**: `admin123`
- **Admin Email**: `admin@demo.com`

---

## Seed Data Summary

### Tenants (2)
1. **ACME Corporation** (`acme`) - Full-featured tenant
2. **Demo Organization** (`demo`) - Testing tenant

### Users (7 total)

#### ACME Users (5)
1. **admin** (System Administrator)
   - Email: admin@acme.com
   - Password: admin123
   - Roles: SUPER_ADMIN, ADMIN, PROCESS_DESIGNER, USER
   - Department: Executive (Manager)

2. **john.doe** (Backend Developer)
   - Email: john.doe@acme.com
   - Password: user123
   - Roles: PROCESS_DESIGNER, USER
   - Department: Backend Team

3. **jane.smith** (Backend Team Lead)
   - Email: jane.smith@acme.com
   - Password: user123
   - Roles: ADMIN, PROCESS_DESIGNER
   - Department: Backend Team (Manager)

4. **bob.wilson** (Frontend Developer)
   - Email: bob.wilson@acme.com
   - Password: user123
   - Roles: USER
   - Department: Frontend Team

5. **alice.brown** (HR Representative)
   - Email: alice.brown@acme.com
   - Password: user123
   - Roles: USER
   - Department: Human Resources

#### Demo Users (2)
1. **demo_admin** (Demo Administrator)
   - Email: admin@demo.com
   - Password: admin123
   - Roles: SUPER_ADMIN, ADMIN, PROCESS_DESIGNER, USER

2. **demo_user** (Demo User)
   - Email: user@demo.com
   - Password: user123
   - Roles: USER

### Roles (4 per tenant)
1. **SUPER_ADMIN** - Full system access (`*` permissions)
2. **ADMIN** - Administrative access within tenant
3. **PROCESS_DESIGNER** - Can deploy process definitions
4. **USER** - Standard user access

### Departments (5 for ACME, 2 for Demo)

#### ACME Departments
- **Executive** (Level 0) - Company leadership
- **Engineering** (Level 0) - Technology department
  - **Backend Team** (Level 1) - Server-side development
  - **Frontend Team** (Level 1) - Client-side development
- **Human Resources** (Level 0) - HR department

#### Demo Departments
- **General** (Level 0) - General operations
- **Development** (Level 0) - Development team

### Form Schemas (2)
1. **Leave Request Form** - For leave approval workflow
   - Version: 1.0.0
   - Process: `leaveRequest`
   - Task: `managerApproval`
   - Fields: startDate, endDate, reason, days

2. **Expense Report Form** - For expense approval workflow
   - Version: 1.0.0
   - Process: `expenseReport`
   - Task: `financeApproval`
   - Fields: expenseDate, amount, category, description, receipt

### Sample BPMN Processes (3)
1. **Leave Request Process** (`leaveRequest`)
   - Simple 3-step approval: Submit → Manager Approval → Complete
   - File: `backend/src/main/resources/processes/samples/leave-request.bpmn20.xml`

2. **Expense Report Process** (`expenseReport`)
   - Multi-step with approval gateway: Submit → Manager Review → Finance Approval
   - File: `backend/src/main/resources/processes/samples/expense-report.bpmn20.xml`

3. **Purchase Order Process** (`purchaseOrder`)
   - Complex workflow with amount-based routing
   - File: `backend/src/main/resources/processes/samples/purchase-order.bpmn20.xml`

---

## Multi-Tenant Isolation Verification

The seed data demonstrates proper multi-tenant isolation:

1. **User Isolation**: Each user belongs to exactly one tenant
2. **Role Isolation**: Roles are tenant-specific
3. **Department Isolation**: Departments exist within tenant boundaries
4. **Data Isolation**: All queries are scoped by `tenant_id`

### Verification Queries

```sql
-- View tenant summary
SELECT * FROM tenant_summary ORDER BY tenant_code;

-- Count users per tenant
SELECT t.code, COUNT(u.id) as user_count
FROM tenants t
LEFT JOIN users u ON u.tenant_id = t.id
GROUP BY t.code;

-- Verify ACME admin has all roles
SELECT r.code
FROM roles r
JOIN user_roles ur ON ur.role_id = r.id
JOIN users u ON u.id = ur.user_id
JOIN tenants t ON t.id = u.tenant_id
WHERE t.code = 'acme' AND u.username = 'admin';

-- List all departments with managers
SELECT
    t.code as tenant_code,
    d.name as department_name,
    u.username as manager_username
FROM departments d
JOIN tenants t ON t.id = d.tenant_id
LEFT JOIN users u ON u.id = d.manager_id
ORDER BY t.code, d.name;
```

---

## Testing Scenarios

### Scenario 1: ACME Admin Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "tenantCode": "acme"
  }'
```

### Scenario 2: Start Leave Request Process
```bash
curl -X POST http://localhost:8080/api/processes \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: 123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "processDefinitionKey": "leaveRequest",
    "businessKey": "LR-2024-001",
    "variables": {
      "employeeName": "john.doe",
      "managerDepartment": "engineering"
    }
  }'
```

### Scenario 3: List Process Instances
```bash
curl -X GET http://localhost:8080/api/processes \
  -H "X-Tenant-Id: 123e4567-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}"
```

---

## Security Notes

⚠️ **IMPORTANT**: These are default credentials for testing/development only.

**For Production**:
1. Change all default passwords immediately
2. Remove or disable test accounts
3. Implement strong password policies
4. Enable multi-factor authentication
5. Review and update role permissions

---

## Troubleshooting

### Issue: Cannot login with default credentials
**Solution**: Verify database migration ran successfully:
```sql
SELECT username, email, is_active FROM users WHERE tenant_id = '123e4567-e89b-12d3-a456-426614174000';
```

### Issue: Multi-tenant isolation not working
**Solution**: Verify `X-Tenant-Id` header is being sent with all API requests

### Issue: Sample processes not available
**Solution**: Deploy sample BPMN files through the admin console or API

---

## Data Reset

To reset seed data and start fresh:
```sql
-- ⚠️ DELETES ALL DATA - Use with caution!
DELETE FROM user_departments;
DELETE FROM user_roles;
DELETE FROM audit_logs;
DELETE FROM form_schemas;
DELETE FROM users;
DELETE FROM departments;
DELETE FROM roles;
DELETE FROM tenants;

-- Then re-run V2__insert_seed_data.sql migration
```

---

## Additional Resources

- **API Documentation**: `specs/001-flowable-platform-core/contracts/api-endpoints.md`
- **Data Model**: `specs/001-flowable-platform-core/data-model.md`
- **Implementation Plan**: `specs/001-flowable-platform-core/plan.md`
- **Quickstart**: `specs/001-flowable-platform-core/quickstart.md`

---

**Seed data version**: 1.0.0 | **Last Updated**: 2026-03-21
