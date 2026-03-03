-- ============================================================
-- IELTS Test Schema - Phase 1: Listening & Reading
-- ============================================================

-- 1. Main test table
CREATE TABLE ielts_tests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(500) NOT NULL,
    skill           VARCHAR(20)  NOT NULL CHECK (skill IN ('LISTENING', 'READING')),
    time_limit_minutes INT       NOT NULL DEFAULT 60,
    difficulty      VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM' CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    is_published    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- 2. Sections (Part 1-4 for Listening, Section 1-3 for Reading)
CREATE TABLE ielts_sections (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    test_id         UUID         NOT NULL REFERENCES ielts_tests(id) ON DELETE CASCADE,
    section_order   INT          NOT NULL,
    title           VARCHAR(500),
    audio_url       VARCHAR(1000),  -- Only used for Listening
    instructions    TEXT,
    UNIQUE (test_id, section_order)
);

-- 3. Passages (Reading passages or Listening question groups)
CREATE TABLE ielts_passages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    section_id      UUID         NOT NULL REFERENCES ielts_sections(id) ON DELETE CASCADE,
    passage_order   INT          NOT NULL,
    title           VARCHAR(500),
    content         TEXT,          -- Full passage text for Reading
    UNIQUE (section_id, passage_order)
);

-- 4. Questions
CREATE TABLE ielts_questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    passage_id      UUID         NOT NULL REFERENCES ielts_passages(id) ON DELETE CASCADE,
    question_order  INT          NOT NULL,
    question_type   VARCHAR(30)  NOT NULL,
    question_text   TEXT         NOT NULL,
    options         JSONB,        -- e.g. ["A. ...", "B. ...", "C. ..."] for MCQ
    correct_answers JSONB        NOT NULL, -- e.g. ["TRUE"] or ["B"] or ["answer text"]
    explanation     TEXT,
    UNIQUE (passage_id, question_order)
);

-- 5. Test attempts (user history)
CREATE TABLE ielts_test_attempts (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    test_id           UUID         NOT NULL REFERENCES ielts_tests(id) ON DELETE CASCADE,
    total_questions   INT          NOT NULL DEFAULT 0,
    correct_count     INT          NOT NULL DEFAULT 0,
    band_score        REAL,
    time_spent_seconds INT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    started_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMPTZ
);

-- 6. Individual answer records
CREATE TABLE ielts_answer_records (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id      UUID         NOT NULL REFERENCES ielts_test_attempts(id) ON DELETE CASCADE,
    question_id     UUID         NOT NULL REFERENCES ielts_questions(id) ON DELETE CASCADE,
    user_answer     JSONB,        -- e.g. ["B"] or ["some text"]
    is_correct      BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (attempt_id, question_id)
);

-- Indexes for performance
CREATE INDEX idx_ielts_tests_skill ON ielts_tests(skill);
CREATE INDEX idx_ielts_tests_published ON ielts_tests(is_published);
CREATE INDEX idx_ielts_sections_test_id ON ielts_sections(test_id);
CREATE INDEX idx_ielts_passages_section_id ON ielts_passages(section_id);
CREATE INDEX idx_ielts_questions_passage_id ON ielts_questions(passage_id);
CREATE INDEX idx_ielts_test_attempts_user_id ON ielts_test_attempts(user_id);
CREATE INDEX idx_ielts_test_attempts_test_id ON ielts_test_attempts(test_id);
CREATE INDEX idx_ielts_answer_records_attempt_id ON ielts_answer_records(attempt_id);
