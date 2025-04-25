package es.in2.issuer.backend.oidc4vci.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialDefinition;
import lombok.Builder;

import java.util.List;
@Builder
public record CredentialConfiguration(@JsonProperty("format") String format,
                                      @JsonProperty("cryptographic_binding_methods_supported") List<String> cryptographicBindingMethodsSupported,
                                      @JsonProperty("credential_signing_alg_values_supported") List<String> credentialSigningAlgValuesSupported,
                                      @JsonProperty("credential_definition") CredentialDefinition credentialDefinition) {
}
