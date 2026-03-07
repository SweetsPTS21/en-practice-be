-- Phase 2: Writing Feature
-- Creates writing_tasks and writing_submissions tables

-- writing_tasks: admin-created writing prompts
CREATE TABLE IF NOT EXISTS writing_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_type VARCHAR(20) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    instruction TEXT,
    image_urls TEXT,
    ai_grading_prompt TEXT,
    difficulty VARCHAR(10) NOT NULL DEFAULT 'MEDIUM',
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    time_limit_minutes INT NOT NULL DEFAULT 60,
    min_words INT NOT NULL DEFAULT 150,
    max_words INT DEFAULT 300,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- writing_submissions: user essay submissions + AI grading results
CREATE TABLE IF NOT EXISTS writing_submissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    task_id UUID NOT NULL REFERENCES writing_tasks(id) ON DELETE CASCADE,
    essay_content TEXT NOT NULL,
    word_count INT NOT NULL DEFAULT 0,
    time_spent_seconds INT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    task_response_score FLOAT,
    coherence_score FLOAT,
    lexical_resource_score FLOAT,
    grammar_score FLOAT,
    overall_band_score FLOAT,
    ai_feedback TEXT,
    submitted_at TIMESTAMPTZ DEFAULT NOW(),
    graded_at TIMESTAMPTZ
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_writing_tasks_task_type ON writing_tasks(task_type);
CREATE INDEX IF NOT EXISTS idx_writing_tasks_difficulty ON writing_tasks(difficulty);
CREATE INDEX IF NOT EXISTS idx_writing_tasks_is_published ON writing_tasks(is_published);
CREATE INDEX IF NOT EXISTS idx_writing_submissions_user_id ON writing_submissions(user_id);
CREATE INDEX IF NOT EXISTS idx_writing_submissions_task_id ON writing_submissions(task_id);
CREATE INDEX IF NOT EXISTS idx_writing_submissions_status ON writing_submissions(status);
