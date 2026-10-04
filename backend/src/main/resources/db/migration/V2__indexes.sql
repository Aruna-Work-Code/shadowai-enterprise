CREATE INDEX idx_request_status ON request(status);
CREATE INDEX idx_investigation_status ON investigation(status);
CREATE INDEX idx_audit_entity ON audit_event(entity_type,entity_id);
CREATE INDEX idx_integration_event_status ON integration_event(processing_status);
