CREATE TABLE user_dictionary (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    -- Core Word Data
    word                VARCHAR(200) NOT NULL,
    ipa                 VARCHAR(200),
    word_type           VARCHAR(50),
    
    -- Meaning & Usage
    meaning             TEXT NOT NULL,
    explanation         TEXT,
    note                TEXT,
    examples            JSONB DEFAULT '[]',
    
    -- Organization & Metadata
    tags                JSONB DEFAULT '[]',
    source_type         VARCHAR(50) DEFAULT 'MANUAL',
    source_reference_id UUID,
    is_favorite         BOOLEAN NOT NULL DEFAULT FALSE,

    -- Learning/SRS Tracking
    proficiency_level   INT NOT NULL DEFAULT 0,
    last_reviewed_at    TIMESTAMPTZ,
    next_review_at      TIMESTAMPTZ,
    review_count        INT NOT NULL DEFAULT 0,

    -- Timestamps
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT uq_user_word UNIQUE (user_id, word)
);

-- Indexes for performance
CREATE INDEX idx_user_dict_user_id ON user_dictionary(user_id);
CREATE INDEX idx_user_dict_word ON user_dictionary(word);
CREATE INDEX idx_user_dict_tags ON user_dictionary USING GIN (tags);
CREATE INDEX idx_user_dict_next_review ON user_dictionary(user_id, next_review_at);
