package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record AppNonceValidationResponse(
        @Schema(example = "eyJhbGciOiJSUzI1Ni...yE1XAP-LrbAtaX-wln0wQgdXfu4dephCsMA", description = "The access token to store") @JsonProperty("accessToken") String accessToken) {
}
