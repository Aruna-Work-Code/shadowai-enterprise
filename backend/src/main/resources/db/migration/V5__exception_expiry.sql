ALTER TABLE exception_request
ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE exception_request
DROP CONSTRAINT IF EXISTS chk_exception_status;

ALTER TABLE exception_request
ADD CONSTRAINT chk_exception_status
CHECK (
    status IN (
        'PENDING_REVIEW',
        'APPROVED',
        'REJECTED',
        'EXPIRED'
    )
);