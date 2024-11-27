package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VerifiableCertificationJwtPayload(
        @JsonProperty("sub")
        String subject,

        @JsonProperty("nbf")
        Long notValidBefore,

        @JsonProperty("iss")
        String issuer,

        @JsonProperty("exp")
        Long expirationTime,

        @JsonProperty("iat")
        Long issuedAt,

        @JsonProperty("jwtCredential")
        VerifiableCertification credential,

        @JsonProperty("jti")
        String JwtId
) {
}
