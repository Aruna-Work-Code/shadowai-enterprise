-- Phase 15 enterprise hardening / Phase 14 completion:
-- tenant ownership, configurable policy metadata and tamper-evident audit fields.
-- Existing data is assigned to the default tenant (id=1).
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'ai_tool','policy','request','exception_request','investigation',
        'integration_connection','integration_event','audit_event','notification'
    ]
    LOOP
        EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS tenant_id BIGINT', t);
        EXECUTE format('UPDATE %I SET tenant_id=1 WHERE tenant_id IS NULL', t);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN tenant_id SET NOT NULL', t);
        EXECUTE format('ALTER TABLE %I ADD CONSTRAINT %I FOREIGN KEY (tenant_id) REFERENCES tenant(id)',
                       t, 'fk_' || t || '_tenant');
    END LOOP;
END $$;

CREATE INDEX IF NOT EXISTS idx_ai_tool_tenant ON ai_tool(tenant_id);
CREATE INDEX IF NOT EXISTS idx_policy_tenant ON policy(tenant_id);
CREATE INDEX IF NOT EXISTS idx_request_tenant ON request(tenant_id);
CREATE INDEX IF NOT EXISTS idx_exception_tenant ON exception_request(tenant_id);
CREATE INDEX IF NOT EXISTS idx_investigation_tenant ON investigation(tenant_id);
CREATE INDEX IF NOT EXISTS idx_integration_connection_tenant ON integration_connection(tenant_id);
CREATE INDEX IF NOT EXISTS idx_integration_event_tenant ON integration_event(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_tenant ON audit_event(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notification_tenant ON notification(tenant_id);

-- Policy management metadata.
ALTER TABLE policy ADD COLUMN IF NOT EXISTS risk_level VARCHAR(20);
ALTER TABLE policy ADD COLUMN IF NOT EXISTS exception_allowed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE policy ADD COLUMN IF NOT EXISTS max_duration_days INTEGER;
ALTER TABLE policy ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE policy ADD COLUMN IF NOT EXISTS description TEXT;

-- Audit integrity chain.
ALTER TABLE audit_event ADD COLUMN IF NOT EXISTS previous_hash VARCHAR(128);
ALTER TABLE audit_event ADD COLUMN IF NOT EXISTS event_hash VARCHAR(128);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_event(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_hash ON audit_event(event_hash);

-- Default membership for all existing users.
INSERT INTO tenant_membership(tenant_id,user_id,role)
SELECT 1,u.id,u.role FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM tenant_membership m
    WHERE m.tenant_id=1 AND m.user_id=u.id
);
