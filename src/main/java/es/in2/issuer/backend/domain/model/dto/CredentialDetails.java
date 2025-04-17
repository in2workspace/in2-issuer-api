package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.UUID;
@Builder
public record CredentialDetails(
        @JsonProperty("procedure_id") UUID procedureId,
        @JsonProperty("credential_status") String credentialStatus,
        @JsonProperty("operation_mode") String operationMode,
        @JsonProperty("signature_mode") String signatureMode,
        @JsonProperty("credential") JsonNode credential
        ) {
}
