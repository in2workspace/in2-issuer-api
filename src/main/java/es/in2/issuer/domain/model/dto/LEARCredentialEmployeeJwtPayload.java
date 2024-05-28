package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record LEARCredentialEmployeeJwtPayload(
        @JsonProperty("sub")
        String subject,

        @JsonProperty("nbf")
        String notValidBefore,

        @JsonProperty("iss")
        String issuer,

        @JsonProperty("exp")
        String expirationTime,

        @JsonProperty("iat")
        String issuedAt,

        @JsonProperty("vc")
        LEARCredentialEmployee learCredentialEmployee,

        @JsonProperty("jti")
        String JwtId
) {
}
