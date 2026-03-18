-- =============================================
-- User Smart Reminders (Pre-computed AI smart reminders)
-- =============================================
CREATE TABLE user_smart_reminders (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    reminder    JSONB DEFAULT '{}',
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_smart_reminders_user_id ON user_smart_reminders(user_id);
