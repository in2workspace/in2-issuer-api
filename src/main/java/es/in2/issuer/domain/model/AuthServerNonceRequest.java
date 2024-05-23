package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record AuthServerNonceRequest(
        @JsonProperty("pre-authorized_code")
        String preAuthorizedCode,

        @JsonProperty("access_token")
        String accessToken
) {
}
