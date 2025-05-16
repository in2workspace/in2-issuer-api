package es.in2.issuer.backend.shared.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import es.in2.issuer.backend.shared.domain.validation.constraint.CredentialConfigurationsSupportedConstraint;
import lombok.Builder;

@Builder
public record PreSubmittedDataCredential(
        @JsonProperty("schema") @CredentialConfigurationsSupportedConstraint String schema,
        @JsonProperty("format") String format,
        @JsonProperty("payload") JsonNode payload,
        @JsonProperty("operation_mode") String operationMode,
        @JsonProperty("validity_period") int validityPeriod,
        @JsonProperty("response_uri") String responseUri
) {
}