package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CredentialRequest(
        @Schema(description = "Request body for creating a Verifiable Credential") @JsonProperty("format") String format,
        @JsonProperty("credential_definition") CredentialDefinition credentialDefinition,
        @JsonProperty("credential_identifier") String credentialIdentifier,
        @JsonProperty("proof") Proof proof) {
}
