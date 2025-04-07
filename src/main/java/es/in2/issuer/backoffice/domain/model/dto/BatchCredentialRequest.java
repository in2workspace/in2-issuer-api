package es.in2.issuer.backoffice.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record BatchCredentialRequest(
        @JsonProperty("credential_requests") List<CredentialRequest> credentialRequests
        ) {
}
