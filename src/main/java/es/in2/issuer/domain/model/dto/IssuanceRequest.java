package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record IssuanceRequest(
        @JsonProperty("schema") String schema,
        @JsonProperty("format") String format,
        @JsonProperty("payload") JsonNode payload,
        @JsonProperty("operationMode") String operationMode,
        @JsonProperty("validity_period") int validityPeriod,
        @JsonProperty("response_uri") String responseUri
) {
}
