-- Logical delete of a conversation from the web UI. The chat row and its
-- messages stay in the database for later analysis. Hibernate also maps this
-- column; IF EXISTS keeps a fresh database working when Flyway runs before
-- the chat table is created.
ALTER TABLE IF EXISTS chat
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
