-- Records whether a message was copied to the clipboard and, for agent
-- replies, which assistant produced it. Hibernate also maps these columns;
-- IF EXISTS keeps a fresh database working when Flyway runs before the
-- message table is created.
ALTER TABLE IF EXISTS message
    ADD COLUMN IF NOT EXISTS copied BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE IF EXISTS message
    ADD COLUMN IF NOT EXISTS agent VARCHAR(32);
