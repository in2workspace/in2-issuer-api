package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = """
        Implements the credential response in jwt format and in cwt format
        """)
public record VerifiableCredentialResponse(
        @Schema(example = "[LUpixVCWJk0eOt4CXQe1NXK....WZwmhmn9OQp6YxX0a2L, 6BF4-LY9O8*T972ZC2D7P5Y....BZIO969A5", description = "Contains issued Credential in jwt and in cwt format") @JsonProperty("credentials") List<VerifiableCredential> credentials) {
}
