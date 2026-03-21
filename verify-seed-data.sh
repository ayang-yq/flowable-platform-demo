#!/bin/bash

# Seed Data Verification Script
# This script verifies that seed data was loaded correctly

echo "=== Seed Data Verification ==="
echo ""

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL..."
sleep 5

# Check if we can connect to PostgreSQL
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "SELECT 'Database connection: OK' as status;" 2>/dev/null || {
    echo "❌ Cannot connect to database"
    exit 1
}

echo "✅ Database connection successful"
echo ""

# Verify tenants
echo "=== Checking Tenants ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    code as tenant_code,
    name as tenant_name,
    is_active
FROM tenants
ORDER BY code;
" 2>/dev/null || echo "Query failed"

echo ""

# Verify users
echo "=== Checking Users ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    t.code as tenant,
    u.username,
    u.email,
    u.is_active
FROM users u
JOIN tenants t ON t.id = u.tenant_id
ORDER BY t.code, u.username;
" 2>/dev/null || echo "Query failed"

echo ""

# Verify roles
echo "=== Checking Roles ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    t.code as tenant,
    r.code as role_code,
    r.is_system
FROM roles r
JOIN tenants t ON t.id = r.tenant_id
ORDER BY t.code, r.code;
" 2>/dev/null || echo "Query failed"

echo ""

# Verify departments
echo "=== Checking Departments ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    t.code as tenant,
    d.name as department,
    d.level,
    (u.username) as manager
FROM departments d
JOIN tenants t ON t.id = d.tenant_id
LEFT JOIN users u ON u.id = d.manager_id
ORDER BY t.code, d.level, d.name;
" 2>/dev/null || echo "Query failed"

echo ""

# Verify user role assignments
echo "=== Checking User-Role Assignments ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    t.code as tenant,
    u.username,
    r.code as role
FROM user_roles ur
JOIN users u ON u.id = ur.user_id
JOIN roles r ON r.id = ur.role_id
JOIN tenants t ON t.id = u.tenant_id
ORDER BY t.code, u.username, r.code;
" 2>/dev/null || echo "Query failed"

echo ""

# Verify form schemas
echo "=== Checking Form Schemas ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    t.code as tenant,
    fs.name as form_name,
    fs.version,
    fs.is_active
FROM form_schemas fs
JOIN tenants t ON t.id = fs.tenant_id
ORDER BY t.code, fs.name;
" 2>/dev/null || echo "Query failed"

echo ""

# Count records
echo "=== Record Counts ==="
docker exec flowable-postgres psql -U flowable -d flowable_platform -c "
SELECT
    'tenants' as entity_type,
    COUNT(*) as count
FROM tenants
UNION ALL
SELECT
    'users' as entity_type,
    COUNT(*) as count
FROM users
UNION ALL
SELECT
    'roles' as entity_type,
    COUNT(*) as count
FROM roles
UNION ALL
SELECT
    'departments' as entity_type,
    COUNT(*) as count
FROM departments
UNION ALL
SELECT
    'user_roles' as entity_type,
    COUNT(*) as count
FROM user_roles
UNION ALL
SELECT
    'user_departments' as entity_type,
    COUNT(*) as count
FROM user_departments
UNION ALL
SELECT
    'form_schemas' as entity_type,
    COUNT(*) as count
FROM form_schemas;
" 2>/dev/null || echo "Query failed"

echo ""
echo "=== Verification Complete ==="
echo ""
echo "Default Credentials:"
echo "  ACME Tenant:"
echo "    Code: acme"
echo "    Admin Username: admin"
echo "    Admin Password: admin123"
echo ""
echo "  Demo Tenant:"
echo "    Code: demo"
echo "    Admin Username: demo_admin"
echo "    Admin Password: admin123"
