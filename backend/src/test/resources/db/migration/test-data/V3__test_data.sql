-- Test Data Migration for Integration Tests
-- This migration creates test tenants and users for API testing

-- ============================================
-- TEST TENANTS
-- ============================================

INSERT INTO tenants (id, code, name, is_active, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'tenant-1', 'Test Tenant 1', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('22222222-2222-2222-2222-222222222222', 'tenant-2', 'Test Tenant 2', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

-- ============================================
-- TEST USERS
-- ============================================
-- Passwords: admin123, user123, other123
-- BCrypt hashes generated using Spring Security BCryptPasswordEncoder
-- admin123: $2a$10$y00dEKkpGuyDX.0qOpRj2utSJAPKQLjkYPg9ENVvAG0uD7hcr3GLq
-- user123: $2a$10$QyzoyZl3RTJ3jVsJOsZh9uJu1ikhGBoSHTqY3hLkSsm5LWBAzlHtO
-- other123: $2a$10$JvOvS0AIf6thAo.cVIUuSu/5WPCRknlW8dxRsfdFQh9zjBmE4vn3m

INSERT INTO users (id, tenant_id, username, email, password, first_name, last_name, display_name, is_active, email_verified, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     '11111111-1111-1111-1111-111111111111',
     'admin',
     'admin@test.com',
     '$2a$10$y00dEKkpGuyDX.0qOpRj2utSJAPKQLjkYPg9ENVvAG0uD7hcr3GLq',
     'Admin',
     'User',
     'Admin User',
     true,
     true,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     '11111111-1111-1111-1111-111111111111',
     'user',
     'user@test.com',
     '$2a$10$QyzoyZl3RTJ3jVsJOsZh9uJu1ikhGBoSHTqY3hLkSsm5LWBAzlHtO',
     'Test',
     'User',
     'Test User',
     true,
     true,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP),
    ('cccccccc-cccc-cccc-cccc-cccccccccccc',
     '22222222-2222-2222-2222-222222222222',
     'other',
     'other@test.com',
     '$2a$10$JvOvS0AIf6thAo.cVIUuSu/5WPCRknlW8dxRsfdFQh9zjBmE4vn3m',
     'Other',
     'User',
     'Other User',
     true,
     true,
     CURRENT_TIMESTAMP,
     CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, username) DO NOTHING;
