package es.in2.issuer.backend.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Schema(description = """
   This data class is used to represent the Credential Offer by Reference using credential_offer_uri parameter for a
   Pre-Authorized Code Flow.\s
   For more information: https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-sending-credential-offer-by-
""")
@Builder
public record CustomCredentialOffer(
        @Schema(example = "https://client-issuer.com")
        @JsonProperty("credential_issuer")
        @NotBlank
        String credentialIssuer,
        @Schema(example = "[\"UniversityDegree\"]")
        @JsonProperty("credential_configuration_ids")
        List<String> credentialConfigurationIds,
        @Schema(example = "[\"UniversityDegree\"]")
        @JsonProperty("credentials")
        List<Credential> credentials, // Remove for DOME profile
        @Schema(implementation = Grant.class)
        @JsonProperty("grants")
        Map<String, Grant> grants
) {
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Credential(
                @JsonProperty("format")
                String format,

                @JsonProperty("types")
                List<String> types
        ){}
}
