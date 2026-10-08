-- Records whether an agent reply activated a skill during the turn.
-- Hibernate also maps this column; IF EXISTS keeps a fresh database working
-- when Flyway runs before the message table is created.
ALTER TABLE IF EXISTS message
    ADD COLUMN IF NOT EXISTS skill_activated BOOLEAN NOT NULL DEFAULT FALSE;
