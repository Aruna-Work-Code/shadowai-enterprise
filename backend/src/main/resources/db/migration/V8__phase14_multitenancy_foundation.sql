CREATE TABLE IF NOT EXISTS tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    key VARCHAR(80) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant_membership (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL REFERENCES tenant(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(40) NOT NULL,
    CONSTRAINT uk_tenant_user UNIQUE (tenant_id, user_id)
);

INSERT INTO tenant(name, key, enabled)
VALUES ('Default Organization', 'default', TRUE)
ON CONFLICT (key) DO NOTHING;
