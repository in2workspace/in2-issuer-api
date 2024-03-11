package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CredentialIssuerMetadata(@JsonProperty("credential_issuer") String credentialIssuer,
                                       @JsonProperty("credential_endpoint") String credentialEndpoint,
                                       @JsonProperty("credential_token") String credentialToken,
                                       @JsonProperty("credentials_supported") List<CredentialsSupported> credentialsSupported) {
}
