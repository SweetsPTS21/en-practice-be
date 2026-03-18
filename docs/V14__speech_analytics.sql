-- V14: Speech analytics columns
-- Adds word-level speech metrics to speaking_attempts and speaking_conversation_turns
-- for pronunciation analysis, pause detection, and speech rate measurement.

-- ─── speaking_attempts ────────────────────────────────────────────────────────
ALTER TABLE speaking_attempts
    ADD COLUMN IF NOT EXISTS word_count            INT,
    ADD COLUMN IF NOT EXISTS words_per_minute      NUMERIC(7, 2),
    ADD COLUMN IF NOT EXISTS pause_count           INT,
    ADD COLUMN IF NOT EXISTS avg_pause_duration_ms NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS long_pause_count      INT,
    ADD COLUMN IF NOT EXISTS filler_word_count     INT,
    ADD COLUMN IF NOT EXISTS avg_word_confidence   NUMERIC(6, 4),
    -- JSON storage for list data (filler words, low-confidence words, per-word details)
    ADD COLUMN IF NOT EXISTS speech_data_json      TEXT;

-- ─── speaking_conversation_turns ─────────────────────────────────────────────
ALTER TABLE speaking_conversation_turns
    ADD COLUMN IF NOT EXISTS word_count            INT,
    ADD COLUMN IF NOT EXISTS words_per_minute      NUMERIC(7, 2),
    ADD COLUMN IF NOT EXISTS pause_count           INT,
    ADD COLUMN IF NOT EXISTS avg_pause_duration_ms NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS long_pause_count      INT,
    ADD COLUMN IF NOT EXISTS filler_word_count     INT,
    ADD COLUMN IF NOT EXISTS avg_word_confidence   NUMERIC(6, 4),
    ADD COLUMN IF NOT EXISTS speech_data_json      TEXT;

-- Index for querying attempts by speech quality
CREATE INDEX IF NOT EXISTS idx_speaking_attempts_wpm
    ON speaking_attempts (words_per_minute)
    WHERE words_per_minute IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_speaking_attempts_avg_confidence
    ON speaking_attempts (avg_word_confidence)
    WHERE avg_word_confidence IS NOT NULL;

