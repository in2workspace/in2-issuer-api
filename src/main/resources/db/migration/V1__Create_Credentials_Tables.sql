CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS credentials;

-- Create credential_management table if it doesn't exist
CREATE TABLE IF NOT EXISTS credentials.credential_procedure (
    procedure_id uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    credential_id uuid,
    credential_format VARCHAR(20),
    credential_decoded TEXT,
    credential_encoded TEXT,
    credential_status VARCHAR(20),
    organization_identifier VARCHAR(255),
    updated_at TIMESTAMP
);

-- Create credential_deferred table if it doesn't exist
CREATE TABLE IF NOT EXISTS credentials.deferred_credential_metadata (
    id uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    transaction_code VARCHAR(255),
    transaction_id VARCHAR(255),
    auth_server_nonce VARCHAR(255),
    procedure_id uuid,
    vc TEXT,
    vc_format VARCHAR(20),
    CONSTRAINT fk_credential_procedure
        FOREIGN KEY (procedure_id)
        REFERENCES credentials.credential_procedure (procedure_id)
        ON DELETE CASCADE
);
