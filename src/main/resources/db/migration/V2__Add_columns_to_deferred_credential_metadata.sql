ALTER TABLE credentials.deferred_credential_metadata
ADD COLUMN operation_mode VARCHAR(20),
ADD COLUMN response_uri VARCHAR(255);