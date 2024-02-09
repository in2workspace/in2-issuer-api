package es.in2.issuer.api.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CredentialIssuerMetadata {

    @JsonProperty("credential_issuer")
    private final String credentialIssuer;

    @JsonProperty("credential_endpoint")
    private final String credentialEndpoint;

    @JsonProperty("credential_token")
    private final String credentialToken;

    @JsonProperty("credentials_supported")
    private final List<CredentialsSupportedParameter> credentialsSupported;
}
