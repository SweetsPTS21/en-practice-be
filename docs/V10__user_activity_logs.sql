CREATE TABLE user_activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    activity_type VARCHAR(50) NOT NULL,
    entity_id UUID,
    entity_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_user_activity_logs_user ON user_activity_logs(user_id);
CREATE INDEX idx_user_activity_logs_created ON user_activity_logs(created_at DESC);
