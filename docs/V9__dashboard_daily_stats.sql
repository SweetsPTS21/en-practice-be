CREATE TABLE dashboard_daily_stats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stat_date DATE NOT NULL UNIQUE,
    total_users BIGINT DEFAULT 0,
    active_users_today BIGINT DEFAULT 0,
    new_users_this_week BIGINT DEFAULT 0,
    total_ielts BIGINT DEFAULT 0,
    published_ielts BIGINT DEFAULT 0,
    total_speaking BIGINT DEFAULT 0,
    published_speaking BIGINT DEFAULT 0,
    total_writing BIGINT DEFAULT 0,
    published_writing BIGINT DEFAULT 0,
    total_attempts BIGINT DEFAULT 0,
    attempts_today BIGINT DEFAULT 0,
    vocab_today BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
