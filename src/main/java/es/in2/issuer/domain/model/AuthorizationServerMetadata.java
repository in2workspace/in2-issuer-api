package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthorizationServerMetadata(
        @JsonProperty("token_endpoint") String tokenEndpoint
) {
}
