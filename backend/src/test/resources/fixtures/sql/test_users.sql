-- Test users for integration testing
-- These users are created in each test via @BeforeEach or transactional fixtures

-- Test Admin User
INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, is_active, email_verified, locale, timezone, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    '00000000-0000-0000-0000-000000000001',
    'test-admin',
    'test-admin@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.MBUiP7mNcYcUO.g2Hi',
    'Test',
    'Admin',
    true,
    true,
    'en',
    'UTC',
    NOW(),
    NOW()
);

-- Test Regular User
INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, is_active, email_verified, locale, timezone, created_at, updated_at)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '00000000-0000-0000-0000-000000000001',
    'test-user',
    'test-user@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.MBUiP7mNcYcUO.g2Hi',
    'Test',
    'User',
    true,
    true,
    'en',
    'UTC',
    NOW(),
    NOW()
);

-- Test Guest User
INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, is_active, email_verified, locale, timezone, created_at, updated_at)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '00000000-0000-0000-0000-000000000001',
    'test-guest',
    'test-guest@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.MBUiP7mNcYcUO.g2Hi',
    'Test',
    'Guest',
    true,
    false,
    'en',
    'UTC',
    NOW(),
    NOW()
);

-- Test Inactive User (for negative testing)
INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, is_active, email_verified, locale, timezone, created_at, updated_at)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    '00000000-0000-0000-0000-000000000001',
    'test-inactive',
    'test-inactive@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.MBUiP7mNcYcUO.g2Hi',
    'Test',
    'Inactive',
    false,
    false,
    'en',
    'UTC',
    NOW(),
    NOW()
);
