CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    action VARCHAR(64) NOT NULL,
    actor_username VARCHAR(100),
    subject_type VARCHAR(32),
    subject_id VARCHAR(64),
    details TEXT,
    correlation_id VARCHAR(64)
);

CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at DESC);
