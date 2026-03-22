-- Test tenant for integration testing
-- This migration creates the dedicated test tenant used by AbstractIntegrationTest

INSERT INTO tenants (id, name, code, domain, settings, is_active, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Test Tenant',
    'test-tenant',
    'test.localhost',
    '{}',
    true,
    NOW(),
    NOW()
);

-- Insert test roles
INSERT INTO roles (id, tenant_id, name, description, created_at, updated_at)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'ADMIN', 'Administrator role for testing', NOW(), NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'USER', 'Standard user role for testing', NOW(), NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'GUEST', 'Guest role for testing', NOW(), NOW());
