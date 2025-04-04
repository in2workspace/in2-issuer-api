package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.in2.issuer.shared.domain.model.dto.CredentialDefinition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record CredentialRequest(
        @Schema(description = "Request body for creating a Verifiable Credential") @JsonProperty("format") String format,
        @JsonProperty("credential_definition") CredentialDefinition credentialDefinition,
        @JsonProperty("proof") Proof proof) {
}
