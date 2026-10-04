CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY,
 username VARCHAR(100) UNIQUE NOT NULL,
 password_hash VARCHAR(255) NOT NULL,
 role VARCHAR(50) NOT NULL,
 department VARCHAR(100),
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_tool (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 category VARCHAR(100),
 capabilities TEXT,
 approval_status VARCHAR(50) NOT NULL,
 risk_level VARCHAR(30) NOT NULL,
 data_policy VARCHAR(1000),
 owner VARCHAR(150)
);

CREATE TABLE policy (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 data_type VARCHAR(100) NOT NULL,
 allowed_tools TEXT,
 prohibited_tools TEXT,
 approval_required BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE license (
 id BIGSERIAL PRIMARY KEY,
 tool_id BIGINT,
 seats_available INT NOT NULL,
 seats_used INT NOT NULL,
 cost NUMERIC(12,2),
 department VARCHAR(100)
);

CREATE TABLE request (
 id BIGSERIAL PRIMARY KEY,
 employee_id BIGINT,
 intent TEXT NOT NULL,
 data_type VARCHAR(100) NOT NULL,
 department VARCHAR(100) NOT NULL,
 frequency VARCHAR(50) NOT NULL,
 requested_tool VARCHAR(150),
 recommended_tool VARCHAR(150),
 recommendation_score INT,
 confidence VARCHAR(30),
 status VARCHAR(50) NOT NULL,
 decision VARCHAR(100),
 decision_reason TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE investigation (
 id BIGSERIAL PRIMARY KEY,
 title VARCHAR(200) NOT NULL,
 tool_name VARCHAR(150),
 department VARCHAR(100),
 risk_level VARCHAR(30),
 owner_id BIGINT,
 status VARCHAR(50) NOT NULL,
 business_context TEXT,
 root_cause TEXT,
 recommended_action TEXT,
 resolution TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 resolved_at TIMESTAMP
);

CREATE TABLE audit_event (
 id BIGSERIAL PRIMARY KEY,
 entity_type VARCHAR(80) NOT NULL,
 entity_id BIGINT NOT NULL,
 action VARCHAR(100) NOT NULL,
 actor_id BIGINT,
 metadata TEXT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE integration_connection (
 id BIGSERIAL PRIMARY KEY,
 name VARCHAR(150) NOT NULL,
 provider VARCHAR(80) NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 webhook_secret_hash VARCHAR(255),
 created_by BIGINT,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE integration_event (
 id BIGSERIAL PRIMARY KEY,
 provider VARCHAR(80) NOT NULL,
 provider_event_id VARCHAR(255) NOT NULL,
 event_type VARCHAR(100) NOT NULL,
 payload TEXT,
 processing_status VARCHAR(30) NOT NULL,
 error_message VARCHAR(1000),
 received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 processed_at TIMESTAMP,
 CONSTRAINT uk_provider_event UNIQUE(provider, provider_event_id)
);
