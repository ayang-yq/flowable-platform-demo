-- Create form_schemas table for Dynamic Form Engine (User Story 3)
-- This table stores form schema definitions with version control

CREATE TABLE IF NOT EXISTS form_schemas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version INT NOT NULL,
    schema TEXT NOT NULL,
    validation_rules TEXT,
    field_permissions TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    process_definition_key VARCHAR(255),
    task_definition_key VARCHAR(255),

    CONSTRAINT fk_form_schemas_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT uq_form_schemas_tenant_name_version UNIQUE (tenant_id, name, version)
);

-- Create index for faster lookups by tenant and name
CREATE INDEX IF NOT EXISTS idx_form_schemas_tenant_name ON form_schemas(tenant_id, name);
CREATE INDEX IF NOT EXISTS idx_form_schemas_process_definition ON form_schemas(process_definition_key, task_definition_key) WHERE process_definition_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_form_schemas_is_active ON form_schemas(is_active) WHERE is_active = TRUE;

-- Add comment for documentation
COMMENT ON TABLE form_schemas IS 'Stores dynamic form schemas with version control for binding forms to workflow process variables';
COMMENT ON COLUMN form_schemas.id IS 'Unique identifier for the form schema';
COMMENT ON COLUMN form_schemas.tenant_id IS 'Foreign key to tenants table for multi-tenancy';
COMMENT ON COLUMN form_schemas.name IS 'Human-readable name of the form';
COMMENT ON COLUMN form_schemas.description IS 'Optional description of the form purpose';
COMMENT ON COLUMN form_schemas.version IS 'Version number for form schema versioning';
COMMENT ON COLUMN form_schemas.schema IS 'JSON schema defining form structure and fields';
COMMENT ON COLUMN form_schemas.validation_rules IS 'JSON validation rules for form fields';
COMMENT ON COLUMN form_schemas.field_permissions IS 'JSON field-level permissions (read-only, required, hidden)';
COMMENT ON COLUMN form_schemas.is_active IS 'Flag indicating if this version is currently active';
COMMENT ON COLUMN form_schemas.created_at IS 'Timestamp when the form schema was created';
COMMENT ON COLUMN form_schemas.updated_at IS 'Timestamp when the form schema was last updated';
COMMENT ON COLUMN form_schemas.created_by IS 'User who created the form schema';
COMMENT ON COLUMN form_schemas.process_definition_key IS 'Optional: link to specific Flowable process definition';
COMMENT ON COLUMN form_schemas.task_definition_key IS 'Optional: link to specific Flowable task definition';
