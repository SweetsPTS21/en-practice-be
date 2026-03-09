CREATE TABLE notification_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT,
    target_type VARCHAR(20) NOT NULL,
    target_role VARCHAR(20),
    recipients_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
