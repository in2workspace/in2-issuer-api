package es.in2.issuer.backoffice.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record LEARCredentialRequest(@JsonProperty("credential") JsonNode credential) {
}
