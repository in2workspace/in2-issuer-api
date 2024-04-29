package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.sql.Timestamp;
import java.util.UUID;

@Builder
public record CredentialItem(
        @JsonProperty("credential_id") UUID credentialId,
        @JsonProperty("credential") String credential,
        @JsonProperty("format") String format,
        @JsonProperty("status") String status,
        @JsonProperty("modified_at") Timestamp modifiedAt
) {
}
