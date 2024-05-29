package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.UUID;
@Builder
public record CredentialDetails(
        @JsonProperty("procedure_id") UUID procedureId,
        @JsonProperty("credential_status") String credentialStatus,
        @JsonProperty("credential") JsonNode credential

        ) {
}
