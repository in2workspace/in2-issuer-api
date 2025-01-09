ALTER TABLE credentials.credential_procedure
ADD COLUMN subject VARCHAR(255),
ADD COLUMN valid_until TIMESTAMP,
ADD COLUMN credential_type VARCHAR(50);