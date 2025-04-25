package es.in2.issuer.backend.oidc4vci.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import java.util.Map;

@Builder
public record CredentialIssuerMetadata(@JsonProperty("credential_issuer") String credentialIssuer,
                                       @JsonProperty("credential_endpoint") String credentialEndpoint,
                                       @JsonProperty("batch_credential_endpoint") String batchCredentialEndpoint,
                                       @JsonProperty("deferred_credential_endpoint") String deferredCredentialEndpoint,
                                       //@JsonProperty("credential_token") String credentialToken, // Remove for DOME profile
                                       @JsonProperty("credential_configurations_supported") Map<String, CredentialConfiguration> credentialConfigurationsSupported
) {
}
