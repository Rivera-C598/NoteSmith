-- Migration script to add tags and pinned columns to notes table
-- Run this if you already have the notes table created

-- Add tags column (stores comma-separated tags)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS tags VARCHAR(500) DEFAULT '';

-- Add pinned column (boolean flag for pinned notes)
ALTER TABLE notes ADD COLUMN IF NOT EXISTS pinned BOOLEAN DEFAULT FALSE;

-- Update the index to prioritize pinned notes
DROP INDEX IF EXISTS idx_user_created;
CREATE INDEX idx_user_pinned_created ON notes(user_id, pinned DESC, created_at DESC);

-- For fresh installations, use this complete schema:
/*
CREATE TABLE IF NOT EXISTS notes (
    id VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    done BOOLEAN DEFAULT FALSE,
    tags VARCHAR(500) DEFAULT '',
    pinned BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_pinned_created (user_id, pinned DESC, created_at DESC)
);
*/
