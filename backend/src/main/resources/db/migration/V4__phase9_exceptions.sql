CREATE TABLE exception_request (
    id BIGSERIAL PRIMARY KEY,

    request_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,

    business_justification TEXT NOT NULL,
    requested_duration_days INTEGER NOT NULL,

    status VARCHAR(40) NOT NULL DEFAULT 'PENDING_REVIEW',

    reviewer_id BIGINT,
    decision_reason TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_exception_request
        FOREIGN KEY (request_id)
        REFERENCES request(id),

    CONSTRAINT chk_exception_duration
        CHECK (requested_duration_days > 0),

    CONSTRAINT chk_exception_status
        CHECK (
            status IN (
                'PENDING_REVIEW',
                'APPROVED',
                'REJECTED'
            )
        )
);

CREATE INDEX idx_exception_request_request_id
    ON exception_request(request_id);

CREATE INDEX idx_exception_request_status
    ON exception_request(status);

CREATE INDEX idx_exception_request_requested_by
    ON exception_request(requested_by);