CREATE TABLE IF NOT EXISTS custom_speaking_conversations
(
    id                     UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    user_id                UUID         NOT NULL REFERENCES users (id),
    title                  VARCHAR(255) NOT NULL,
    topic                  TEXT         NOT NULL,
    style                  VARCHAR(30)  NOT NULL,
    personality            VARCHAR(30)  NOT NULL,
    voice_name             VARCHAR(30)  NOT NULL,
    expertise              VARCHAR(30)  NOT NULL,
    grading_enabled        BOOLEAN      NOT NULL DEFAULT FALSE,
    status                 VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    max_user_turns         INT          NOT NULL,
    user_turn_count        INT          NOT NULL DEFAULT 0,
    total_turns            INT          NOT NULL DEFAULT 0,
    time_spent_seconds     INT,
    fluency_score          FLOAT,
    vocabulary_score       FLOAT,
    coherence_score        FLOAT,
    pronunciation_score    FLOAT,
    overall_score          FLOAT,
    ai_feedback            TEXT,
    system_prompt_snapshot TEXT,
    started_at             TIMESTAMPTZ           DEFAULT NOW(),
    completed_at           TIMESTAMPTZ,
    graded_at              TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS custom_speaking_conversation_turns
(
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id       UUID NOT NULL REFERENCES custom_speaking_conversations (id) ON DELETE CASCADE,
    turn_number           INT  NOT NULL,
    ai_message            TEXT NOT NULL,
    user_transcript       TEXT,
    audio_url             TEXT,
    time_spent_seconds    INT,
    word_count            INT,
    words_per_minute      NUMERIC(7, 2),
    pause_count           INT,
    avg_pause_duration_ms NUMERIC(10, 2),
    long_pause_count      INT,
    filler_word_count     INT,
    avg_word_confidence   NUMERIC(6, 4),
    speech_data_json      TEXT,
    created_at            TIMESTAMPTZ      DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_custom_speaking_conv_user_id
    ON custom_speaking_conversations (user_id);

CREATE INDEX IF NOT EXISTS idx_custom_speaking_conv_status
    ON custom_speaking_conversations (status);

CREATE INDEX IF NOT EXISTS idx_custom_speaking_conv_turns_conv_id
    ON custom_speaking_conversation_turns (conversation_id);
