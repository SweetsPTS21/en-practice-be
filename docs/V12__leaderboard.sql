-- =============================================
-- V12: Leaderboard & XP System
-- PostgreSQL
-- =============================================

-- 1. Thêm total_xp vào bảng users
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_xp INT NOT NULL DEFAULT 0;

-- 2. Bảng ghi nhận XP activity
CREATE TABLE user_xp_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    source          VARCHAR(50) NOT NULL,
    source_id       VARCHAR(100),
    xp_amount       INT NOT NULL DEFAULT 0,
    earned_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_xp_logs_user_earned ON user_xp_logs(user_id, earned_at DESC);
CREATE INDEX idx_xp_logs_source ON user_xp_logs(source, source_id);

-- 3. Bảng snapshot leaderboard (pre-computed)
CREATE TABLE leaderboard_snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period_type     VARCHAR(20) NOT NULL,   -- WEEKLY, MONTHLY, ALL_TIME
    period_key      VARCHAR(20) NOT NULL,   -- 2025-W03, 2025-01, ALL
    scope           VARCHAR(30) NOT NULL DEFAULT 'GLOBAL',
    xp              INT NOT NULL DEFAULT 0,
    rank            INT NOT NULL DEFAULT 0,
    previous_rank   INT,
    snapshot_date   DATE NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_snapshot
    ON leaderboard_snapshots(user_id, period_type, period_key, scope, snapshot_date);
CREATE INDEX idx_leaderboard_period_scope
    ON leaderboard_snapshots(period_type, period_key, scope, rank);
CREATE INDEX idx_leaderboard_user
    ON leaderboard_snapshots(user_id, period_type, period_key);

-- 4. Bảng tracking XP cap hàng ngày
CREATE TABLE user_daily_xp_cap (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date            DATE NOT NULL,
    total_xp_earned INT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_user_daily_xp ON user_daily_xp_cap(user_id, date);
