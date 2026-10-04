CREATE TABLE IF NOT EXISTS refresh_token (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT NOT NULL REFERENCES users(id),
 tenant_id BIGINT NOT NULL REFERENCES tenant(id),
 token_hash VARCHAR(64) NOT NULL UNIQUE,
 expires_at TIMESTAMP NOT NULL,
 revoked BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 rotated_from_id BIGINT NULL REFERENCES refresh_token(id)
);
CREATE INDEX IF NOT EXISTS idx_refresh_token_hash ON refresh_token(token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_token(user_id,tenant_id);
