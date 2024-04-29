CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS credentials;

-- Create credential_management table if it doesn't exist
CREATE TABLE IF NOT EXISTS credentials.credential_management (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255),
    credential_format VARCHAR(255),
    credential_decoded TEXT,
    credential_encoded TEXT,
    credential_status VARCHAR(255),
    modified_at TIMESTAMP
);

-- Create credential_deferred table if it doesn't exist
CREATE TABLE IF NOT EXISTS credentials.credential_deferred (
    id UUID PRIMARY KEY,
    transaction_id VARCHAR(255),
    credential_id UUID,
    credential_signed TEXT,
    CONSTRAINT fk_credential_management
        FOREIGN KEY (credential_id)
        REFERENCES credentials.credential_management (id)
        ON DELETE CASCADE
);
