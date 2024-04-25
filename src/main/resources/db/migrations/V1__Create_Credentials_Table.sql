CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SCHEMA IF NOT EXISTS credentials;

CREATE TABLE IF NOT EXISTS credentials.credentials (
    id uuid PRIMARY KEY UNIQUE DEFAULT uuid_generate_v4(),
    user_id VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    phone_number VARCHAR(255),
    credential_data TEXT,
    status VARCHAR(255),
    transaction_id VARCHAR(255),
    created_at TIMESTAMP,
    modified_at TIMESTAMP
);