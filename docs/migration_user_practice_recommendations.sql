-- =============================================
-- User Practice Recommendations (Pre-computed)
-- =============================================
CREATE TABLE user_practice_recommendations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    weak_skills     JSONB DEFAULT '[]',
    recommendations JSONB DEFAULT '[]',
    computed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_upr_user_id ON user_practice_recommendations(user_id);
