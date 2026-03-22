-- Collaboration tables for comments and attachments

CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    task_id VARCHAR(255),
    process_instance_id VARCHAR(255),
    mentions JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE INDEX idx_comment_task ON comments(task_id);
CREATE INDEX idx_comment_instance ON comments(process_instance_id);
CREATE INDEX idx_comment_author ON comments(author_id);
CREATE INDEX idx_comment_tenant ON comments(tenant_id);

CREATE TABLE attachments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'local',
    uploader_id UUID NOT NULL,
    task_id VARCHAR(255),
    process_instance_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachment_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_attachment_uploader FOREIGN KEY (uploader_id) REFERENCES users(id)
);

CREATE INDEX idx_attachment_task ON attachments(task_id);
CREATE INDEX idx_attachment_instance ON attachments(process_instance_id);
CREATE INDEX idx_attachment_uploader ON attachments(uploader_id);
CREATE INDEX idx_attachment_tenant ON attachments(tenant_id);

CREATE TRIGGER update_comments_updated_at BEFORE UPDATE ON comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
