package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.Map;

@Builder
public record CredentialIssuerMetadata(@JsonProperty("credential_issuer") String credentialIssuer,
                                       @JsonProperty("credential_endpoint") String credentialEndpoint,
                                       @JsonProperty("batch_credential_endpoint") String batchCredentialEndpoint,
                                       @JsonProperty("credential_token") String credentialToken,
                                       @JsonProperty("credential_configurations_supported") Map<String, CredentialConfiguration> credentialConfigurationsSupported
) {
}
