package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

public record BatchCredentialResponse(
        @JsonProperty("credential_responses") List<CredentialResponse> credentialResponses) {
    @Builder
    public record CredentialResponse(
            @JsonProperty("credential") String credential
    ) {
    }
}