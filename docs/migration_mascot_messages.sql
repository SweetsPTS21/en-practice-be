-- =============================================
-- Mascot Messages (Pre-computed AI encouragements)
-- =============================================
CREATE TABLE mascot_messages (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    messages    JSONB DEFAULT '[]',
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mascot_messages_user_id ON mascot_messages(user_id);
