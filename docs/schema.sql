-- =============================================
-- EN Practice - Database Schema
-- PostgreSQL
-- =============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- 1. USERS
-- =============================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- 2. VOCABULARY RECORDS
-- Mỗi lần user kiểm tra 1 từ = 1 record
-- =============================================
CREATE TABLE vocabulary_records (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    english_word    VARCHAR(255) NOT NULL,
    user_meaning    TEXT NOT NULL,
    correct_meaning TEXT NOT NULL,
    alternatives    JSONB DEFAULT '[]',
    synonyms        JSONB DEFAULT '[]',
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    tested_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- 3. REVIEW SESSIONS
-- Mỗi phiên ôn tập (nhiều từ) = 1 session
-- =============================================
CREATE TABLE review_sessions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    filter          VARCHAR(20) NOT NULL DEFAULT 'all',
    total           INTEGER NOT NULL DEFAULT 0,
    correct         INTEGER NOT NULL DEFAULT 0,
    incorrect       INTEGER NOT NULL DEFAULT 0,
    accuracy        INTEGER NOT NULL DEFAULT 0,
    words           JSONB DEFAULT '[]',
    reviewed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =============================================
-- INDEXES
-- =============================================

-- Truy vấn lịch sử theo thời gian (History page, chart data)
CREATE INDEX idx_vocab_user_tested ON vocabulary_records(user_id, tested_at DESC);

-- Truy vấn từ vựng theo word (review, dedup)
CREATE INDEX idx_vocab_user_word ON vocabulary_records(user_id, english_word);

-- Lọc từ sai (review "từ hay sai", stats)
CREATE INDEX idx_vocab_user_correct ON vocabulary_records(user_id, is_correct);

-- Phiên review gần nhất
CREATE INDEX idx_review_user_date ON review_sessions(user_id, reviewed_at DESC);

-- =============================================
-- TRIGGER: auto-update updated_at on users
-- =============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
