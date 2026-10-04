CREATE TABLE notification (
 id BIGSERIAL PRIMARY KEY,
 user_id BIGINT,
 type VARCHAR(50) NOT NULL,
 title VARCHAR(200) NOT NULL,
 message TEXT NOT NULL,
 read_flag BOOLEAN NOT NULL DEFAULT FALSE,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notification_user_created ON notification(user_id, created_at DESC);
