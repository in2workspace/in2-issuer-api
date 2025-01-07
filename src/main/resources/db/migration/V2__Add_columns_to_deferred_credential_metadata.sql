ALTER TABLE credentials.deferred_credential_metadata
ADD COLUMN operation_mode VARCHAR(20),
ADD COLUMN response_uri VARCHAR(255);
ALTER TABLE credentials.credential_procedure
ADD COLUMN subject VARCHAR(255),
ADD COLUMN credential_type VARCHAR(50);