-- Phase 3b: Speaking Conversation Feature
-- Adds tables for multi-turn conversational speaking practice

CREATE TABLE IF NOT EXISTS speaking_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    topic_id UUID NOT NULL REFERENCES speaking_topics(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_turns INT DEFAULT 0,
    time_spent_seconds INT,
    fluency_score FLOAT,
    lexical_score FLOAT,
    grammar_score FLOAT,
    pronunciation_score FLOAT,
    overall_band_score FLOAT,
    ai_feedback TEXT,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ,
    graded_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS speaking_conversation_turns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES speaking_conversations(id) ON DELETE CASCADE,
    turn_number INT NOT NULL,
    ai_question TEXT NOT NULL,
    user_transcript TEXT,
    audio_url TEXT,
    time_spent_seconds INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_speaking_conv_user_id ON speaking_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_speaking_conv_topic_id ON speaking_conversations(topic_id);
CREATE INDEX IF NOT EXISTS idx_speaking_conv_status ON speaking_conversations(status);
CREATE INDEX IF NOT EXISTS idx_speaking_conv_turns_conv_id ON speaking_conversation_turns(conversation_id);
