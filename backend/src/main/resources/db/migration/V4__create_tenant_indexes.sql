-- Additional composite indexes for multi-tenant query optimization
-- These indexes complement the ones created in V1__create_core_tables.sql

-- User table additional indexes
CREATE INDEX IF NOT EXISTS idx_user_tenant_display_name ON users(tenant_id, display_name);
CREATE INDEX IF NOT EXISTS idx_user_last_login ON users(last_login_at DESC);

-- Role table additional indexes
CREATE INDEX IF NOT EXISTS idx_role_tenant_system ON roles(tenant_id, is_system);

-- Department table additional indexes
CREATE INDEX IF NOT EXISTS idx_dept_tenant_active ON departments(tenant_id, is_active);
CREATE INDEX IF NOT EXISTS idx_dept_tenant_level ON departments(tenant_id, level);

-- Form schema additional indexes
CREATE INDEX IF NOT EXISTS idx_form_tenant_name ON form_schemas(tenant_id, name);

-- Audit log additional indexes
CREATE INDEX IF NOT EXISTS idx_audit_tenant_timestamp ON audit_logs(tenant_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
