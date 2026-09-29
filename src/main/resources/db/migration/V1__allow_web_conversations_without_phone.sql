-- WhatsApp conversations are identified by phone_number, while authenticated
-- web conversations are identified by orion_user_hash. Existing databases were
-- created before the web channel and still require phone_number.
ALTER TABLE IF EXISTS chat
    ALTER COLUMN phone_number DROP NOT NULL;
