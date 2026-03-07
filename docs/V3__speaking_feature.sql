-- Phase 3: Speaking Feature
-- Creates speaking_topics and speaking_attempts tables

-- speaking_topics: admin-created speaking questions
CREATE TABLE IF NOT EXISTS speaking_topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    part VARCHAR(10) NOT NULL,
    question TEXT NOT NULL,
    cue_card TEXT,
    follow_up_questions TEXT,
    ai_grading_prompt TEXT,
    difficulty VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- speaking_attempts: user recordings + AI grading results
CREATE TABLE IF NOT EXISTS speaking_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    topic_id UUID NOT NULL REFERENCES speaking_topics(id) ON DELETE CASCADE,
    audio_url TEXT,
    transcript TEXT,
    time_spent_seconds INT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    fluency_score FLOAT,
    lexical_score FLOAT,
    grammar_score FLOAT,
    pronunciation_score FLOAT,
    overall_band_score FLOAT,
    ai_feedback TEXT,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    graded_at TIMESTAMPTZ
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_speaking_topics_part ON speaking_topics(part);
CREATE INDEX IF NOT EXISTS idx_speaking_topics_difficulty ON speaking_topics(difficulty);
CREATE INDEX IF NOT EXISTS idx_speaking_topics_is_published ON speaking_topics(is_published);
CREATE INDEX IF NOT EXISTS idx_speaking_attempts_user_id ON speaking_attempts(user_id);
CREATE INDEX IF NOT EXISTS idx_speaking_attempts_topic_id ON speaking_attempts(topic_id);
CREATE INDEX IF NOT EXISTS idx_speaking_attempts_status ON speaking_attempts(status);
