package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public record BatchCredentialResponse(
        @JsonProperty("credential_responses") List<CredentialResponse> credentialResponses,
        @JsonProperty("c_nonce") String cNonce,
        @JsonProperty("c_nonce_expires_in") int cNonceExpiresIn) {
    @Builder
    public record CredentialResponse(
            @JsonProperty("credential") String credential
    ) {
    }
}
