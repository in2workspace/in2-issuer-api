package es.in2.issuer.backoffice.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Date;
import java.util.UUID;

@Builder
public record CommitCredential(@JsonProperty("credential_id") UUID credentialId,
                               @JsonProperty("gicar_id") String gicarId,
                               @JsonProperty("expiration_date") Date expirationDate) {
}
