package es.in2.issuer.oidc4vci.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthorizationServerMetadata(
        @JsonProperty("token_endpoint") String tokenEndpoint
) {
}
