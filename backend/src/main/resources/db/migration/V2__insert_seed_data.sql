-- Seed Data for Flowable Platform
-- This migration creates initial tenants, users, roles, and departments

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- TENANTS
-- ============================================

INSERT INTO tenants (id, name, code, domain, logo_url, settings, is_active, created_at, updated_at)
VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 'ACME Corporation', 'acme', 'acme.flowable-platform.com', null, '{"timezone": "UTC", "locale": "en"}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('123e4567-e89b-12d3-a456-426614174001', 'Demo Organization', 'demo', 'demo.flowable-platform.com', null, '{"timezone": "UTC", "locale": "en"}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- ROLES
-- ============================================

-- System roles for ACME tenant
INSERT INTO roles (id, tenant_id, name, code, description, permissions, is_system, created_at, updated_at)
VALUES
    ('223e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174000', 'Super Administrator', 'SUPER_ADMIN', 'Full system access with all permissions', '{"permissions": ["*"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', 'Administrator', 'ADMIN', 'Administrative access within tenant', '{"permissions": ["user.manage", "role.manage", "department.manage", "process.manage", "task.manage.all"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174002', '123e4567-e89b-12d3-a456-426614174000', 'Process Designer', 'PROCESS_DESIGNER', 'Can deploy and manage process definitions', '{"permissions": ["process.deploy", "process.manage"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174003', '123e4567-e89b-12d3-a456-426614174000', 'Standard User', 'USER', 'Standard user access', '{"permissions": ["task.own", "process.start"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- System roles for Demo tenant
INSERT INTO roles (id, tenant_id, name, code, description, permissions, is_system, created_at, updated_at)
VALUES
    ('223e4567-e89b-12d3-a456-426614174010', '123e4567-e89b-12d3-a456-426614174001', 'Super Administrator', 'SUPER_ADMIN', 'Full system access with all permissions', '{"permissions": ["*"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174011', '123e4567-e89b-12d3-a456-426614174001', 'Administrator', 'ADMIN', 'Administrative access within tenant', '{"permissions": ["user.manage", "role.manage", "department.manage", "process.manage", "task.manage.all"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174012', '123e4567-e89b-12d3-a456-426614174001', 'Process Designer', 'PROCESS_DESIGNER', 'Can deploy and manage process definitions', '{"permissions": ["process.deploy", "process.manage"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('223e4567-e89b-12d3-a456-426614174013', '123e4567-e89b-12d3-a456-426614174001', 'Standard User', 'USER', 'Standard user access', '{"permissions": ["task.own", "process.start"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- ============================================
-- DEPARTMENTS
-- ============================================

-- ACME departments
INSERT INTO departments (id, tenant_id, parent_id, name, code, manager_id, path, level, is_active, created_at, updated_at)
VALUES
    ('323e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174000', null, 'Executive', 'executive', null, '/executive/', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('323e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', null, 'Engineering', 'engineering', null, '/engineering/', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('323e4567-e89b-12d3-a456-426614174002', '123e4567-e89b-12d3-a456-426614174000', null, 'Human Resources', 'hr', null, '/hr/', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('323e4567-e89b-12d3-a456-426614174003', '123e4567-e89b-12d3-a456-426614174000', '323e4567-e89b-12d3-a456-426614174001', 'Backend Team', 'backend-team', null, '/engineering/backend-team/', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('323e4567-e89b-12d3-a456-426614174004', '123e4567-e89b-12d3-a456-426614174000', '323e4567-e89b-12d3-a456-426614174001', 'Frontend Team', 'frontend-team', null, '/engineering/frontend-team/', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- Demo departments
INSERT INTO departments (id, tenant_id, parent_id, name, code, manager_id, path, level, is_active, created_at, updated_at)
VALUES
    ('323e4567-e89b-12d3-a456-426614174010', '123e4567-e89b-12d3-a456-426614174001', null, 'General', 'general', null, '/general/', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('323e4567-e89b-12d3-a456-426614174011', '123e4567-e89b-12d3-a456-426614174001', null, 'Development', 'development', null, '/development/', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- ============================================
-- USERS
-- ============================================

-- ACME users (password is admin123 for admin users, user123 for regular users)
-- BCrypt hash of 'admin123' is $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- BCrypt hash of 'user123' is $2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW

INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, display_name, phone, locale, timezone, is_active, email_verified, created_at, updated_at)
VALUES
    -- ACME Admin users
    ('423e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174000', 'admin', 'admin@acme.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System', 'Administrator', 'System Administrator', '+1-555-0100', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- ACME regular users
    ('423e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', 'john.doe', 'john.doe@acme.com', '$2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW', 'John', 'Doe', 'John Doe', '+1-555-0101', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174002', '123e4567-e89b-12d3-a456-426614174000', 'jane.smith', 'jane.smith@acme.com', '$2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW', 'Jane', 'Smith', 'Jane Smith', '+1-555-0102', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174003', '123e4567-e89b-12d3-a456-426614174000', 'bob.wilson', 'bob.wilson@acme.com', '$2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW', 'Bob', 'Wilson', 'Bob Wilson', '+1-555-0103', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174004', '123e4567-e89b-12d3-a456-426614174000', 'alice.brown', 'alice.brown@acme.com', '$2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW', 'Alice', 'Brown', 'Alice Brown', '+1-555-0104', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Demo tenant users
    ('423e4567-e89b-12d3-a456-426614174010', '123e4567-e89b-12d3-a456-426614174001', 'demo_admin', 'admin@demo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo', 'Administrator', 'Demo Administrator', '+1-555-0200', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174011', '123e4567-e89b-12d3-a456-426614174001', 'demo_user', 'user@demo.com', '$2a$10$Y8xYqxHYYqY5f5h.EYqG9OOBpHg3h/GxfQzhWqJx8hqz2h9hBf8uW', 'Demo', 'User', 'Demo User', '+1-555-0201', 'en', 'UTC', true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, username) DO NOTHING;

-- ============================================
-- USER-ROLE ASSIGNMENTS
-- ============================================

-- ACME user roles
INSERT INTO user_roles (user_id, role_id, assigned_at)
VALUES
    -- Admin has all roles
    ('423e4567-e89b-12d3-a456-426614174000', '223e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174000', '223e4567-e89b-12d3-a456-426614174001', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174000', '223e4567-e89b-12d3-a456-426614174002', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174000', '223e4567-e89b-12d3-a456-426614174003', CURRENT_TIMESTAMP),

    -- John Doe: Standard user + Process designer
    ('423e4567-e89b-12d3-a456-426614174001', '223e4567-e89b-12d3-a456-426614174002', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174001', '223e4567-e89b-12d3-a456-426614174003', CURRENT_TIMESTAMP),

    -- Jane Smith: Admin + Process designer
    ('423e4567-e89b-12d3-a456-426614174002', '223e4567-e89b-12d3-a456-426614174001', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174002', '223e4567-e89b-12d3-a456-426614174002', CURRENT_TIMESTAMP),

    -- Bob Wilson: Standard user
    ('423e4567-e89b-12d3-a456-426614174003', '223e4567-e89b-12d3-a456-426614174003', CURRENT_TIMESTAMP),

    -- Alice Brown: Standard user
    ('423e4567-e89b-12d3-a456-426614174004', '223e4567-e89b-12d3-a456-426614174003', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Demo tenant user roles
INSERT INTO user_roles (user_id, role_id, assigned_at)
VALUES
    ('423e4567-e89b-12d3-a456-426614174010', '223e4567-e89b-12d3-a456-426614174010', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174010', '223e4567-e89b-12d3-a456-426614174011', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174010', '223e4567-e89b-12d3-a456-426614174012', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174010', '223e4567-e89b-12d3-a456-426614174013', CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174011', '223e4567-e89b-12d3-a456-426614174013', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================
-- USER-DEPARTMENT ASSIGNMENTS
-- ============================================

-- ACME user departments
INSERT INTO user_departments (user_id, department_id, is_manager, joined_at)
VALUES
    -- Admin in Executive department (as manager)
    ('423e4567-e89b-12d3-a456-426614174000', '323e4567-e89b-12d3-a456-426614174000', true, CURRENT_TIMESTAMP),

    -- John Doe and Jane Smith in Backend Team
    ('423e4567-e89b-12d3-a456-426614174001', '323e4567-e89b-12d3-a456-426614174003', false, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174002', '323e4567-e89b-12d3-a456-426614174003', true, CURRENT_TIMESTAMP),

    -- Bob Wilson in Frontend Team
    ('423e4567-e89b-12d3-a456-426614174003', '323e4567-e89b-12d3-a456-426614174004', false, CURRENT_TIMESTAMP),

    -- Alice Brown in HR
    ('423e4567-e89b-12d3-a456-426614174004', '323e4567-e89b-12d3-a456-426614174002', false, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Demo tenant user departments
INSERT INTO user_departments (user_id, department_id, is_manager, joined_at)
VALUES
    ('423e4567-e89b-12d3-a456-426614174010', '323e4567-e89b-12d3-a456-426614174010', true, CURRENT_TIMESTAMP),
    ('423e4567-e89b-12d3-a456-426614174011', '323e4567-e89b-12d3-a456-426614174011', false, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Update department managers
UPDATE departments SET manager_id = '423e4567-e89b-12d3-a456-426614174000' WHERE id = '323e4567-e89b-12d3-a456-426614174000';
UPDATE departments SET manager_id = '423e4567-e89b-12d3-a456-426614174002' WHERE id = '323e4567-e89b-12d3-a456-426614174003';
UPDATE departments SET manager_id = '423e4567-e89b-12d3-a456-426614174010' WHERE id = '323e4567-e89b-12d3-a456-426614174010';

-- ============================================
-- SAMPLE FORM SCHEMAS
-- ============================================

-- Leave Request Form
INSERT INTO form_schemas (id, tenant_id, name, description, version, process_definition_key, task_definition_key, schema_json, process_variable_mapping, is_active, created_by, created_at, updated_at)
VALUES
    ('523e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174000', 'Leave Request Form', 'Form for submitting leave requests', '1.0.0', 'leaveRequest', 'managerApproval',
    '{
        "title": "Leave Request",
        "description": "Submit a leave request for approval",
        "logoPosition": "right",
        "pages": [
            {
                "name": "page1",
                "elements": [
                    {
                        "type": "text",
                        "name": "startDate",
                        "title": "Start Date",
                        "isRequired": true,
                        "inputType": "date"
                    },
                    {
                        "type": "text",
                        "name": "endDate",
                        "title": "End Date",
                        "isRequired": true,
                        "inputType": "date"
                    },
                    {
                        "type": "text",
                        "name": "reason",
                        "title": "Reason",
                        "isRequired": true,
                        "inputType": "text",
                        "maxLength": 500
                    },
                    {
                        "type": "text",
                        "name": "days",
                        "title": "Number of Days",
                        "isRequired": true,
                        "inputType": "number",
                        "min": 1,
                        "max": 365
                    }
                ]
            }
        ]
    }',
    '{"startDate": "leaveStartDate", "endDate": "leaveEndDate", "reason": "leaveReason", "days": "leaveDays"}',
    true, '423e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Expense Report Form
INSERT INTO form_schemas (id, tenant_id, name, description, version, process_definition_key, task_definition_key, schema_json, process_variable_mapping, is_active, created_by, created_at, updated_at)
VALUES
    ('523e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', 'Expense Report Form', 'Form for submitting expense reports', '1.0.0', 'expenseReport', 'financeApproval',
    '{
        "title": "Expense Report",
        "description": "Submit an expense report for approval",
        "logoPosition": "right",
        "pages": [
            {
                "name": "page1",
                "elements": [
                    {
                        "type": "text",
                        "name": "expenseDate",
                        "title": "Expense Date",
                        "isRequired": true,
                        "inputType": "date"
                    },
                    {
                        "type": "text",
                        "name": "amount",
                        "title": "Amount",
                        "isRequired": true,
                        "inputType": "number",
                        "min": 0
                    },
                    {
                        "type": "text",
                        "name": "category",
                        "title": "Category",
                        "isRequired": true,
                        "inputType": "dropdown",
                        "choices": [
                            "Travel",
                            "Meals",
                            "Office Supplies",
                            "Training",
                            "Other"
                        ]
                    },
                    {
                        "type": "comment",
                        "name": "description",
                        "title": "Description",
                        "isRequired": true,
                        "maxLength": 1000
                    },
                    {
                        "type": "file",
                        "name": "receipt",
                        "title": "Receipt",
                        "isRequired": false,
                        "maxSize": 10485760
                    }
                ]
            }
        ]
    }',
    '{"expenseDate": "expenseDate", "amount": "expenseAmount", "category": "expenseCategory", "description": "expenseDescription"}',
    true, '423e4567-e89b-12d3-a456-426614174000', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================
-- AUDIT LOG ENTRIES (seed data setup)
-- ============================================

INSERT INTO audit_logs (id, tenant_id, user_id, username, timestamp, action_type, entity_type, entity_id, details, created_at)
VALUES
    ('623e4567-e89b-12d3-a456-426614174000', '123e4567-e89b-12d3-a456-426614174000', '423e4567-e89b-12d3-a456-426614174000', 'admin', CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'TENANT', '123e4567-e89b-12d3-a456-426614174000', '{"message": "Initial tenant setup completed"}', CURRENT_TIMESTAMP),
    ('623e4567-e89b-12d3-a456-426614174001', '123e4567-e89b-12d3-a456-426614174000', '423e4567-e89b-12d3-a456-426614174000', 'admin', CURRENT_TIMESTAMP, 'SYSTEM_INIT', 'USER', '423e4567-e89b-12d3-a456-426614174000', '{"message": "Admin user created"}', CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- ============================================
-- TENANT ISOLATION VERIFICATION
-- ============================================

-- Verify tenant isolation (this can be used for testing)
-- Each tenant's users should only see their own data
CREATE OR REPLACE VIEW tenant_summary AS
SELECT
    t.id as tenant_id,
    t.code as tenant_code,
    t.name as tenant_name,
    COUNT(DISTINCT u.id) as user_count,
    COUNT(DISTINCT r.id) as role_count,
    COUNT(DISTINCT d.id) as department_count
FROM tenants t
LEFT JOIN users u ON u.tenant_id = t.id
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
LEFT JOIN user_departments ud ON ud.user_id = u.id
LEFT JOIN departments d ON d.id = ud.department_id
GROUP BY t.id, t.code, t.name;

-- ============================================
-- SEED DATA COMPLETE
-- ============================================

-- Display summary
SELECT 'Seed data insertion completed successfully!' as status;

-- Show tenant summary
SELECT * FROM tenant_summary ORDER BY tenant_code;
