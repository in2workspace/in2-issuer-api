package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record CredentialData(@JsonProperty("credential") JsonNode credential) {
}
