package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Schema(description = """
   This data class is used to represent the Credential Offer by Reference using credential_offer_uri parameter for a
   Pre-Authorized Code Flow.\s
   For more information: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-sending-credential-offer-by-
""")
@Builder
public record CredentialOfferForPreAuthorizedCodeFlow(
        @Schema(example = "https://credential-issuer.example.com") @JsonProperty("credential_issuer") String credentialIssuer,
        @Schema(example = "[\"UniversityDegree\"]") @JsonProperty("credentials") List<String> credentials,
        @Schema(implementation = Grant.class) @JsonProperty("grants") Map<String, Grant> grants) {
}
