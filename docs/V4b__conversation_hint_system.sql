-- V4b: Add turn_type column for adaptive hint system
ALTER TABLE speaking_conversation_turns ADD COLUMN IF NOT EXISTS turn_type VARCHAR(20) DEFAULT 'QUESTION';
-- Also track which follow-up index this QUESTION turn is addressing
ALTER TABLE speaking_conversation_turns ADD COLUMN IF NOT EXISTS follow_up_index INT;
