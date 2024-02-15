package es.in2.issuer.api.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import id.walt.credentials.w3c.templates.VcTemplate;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CredentialsSupportedParameter {

    @JsonProperty("format")
    private final String format;

    @JsonProperty("id")
    private final String id;

    @JsonProperty("types")
    private final List<String> types;

    @JsonProperty("cryptographic_binding_methods_supported")
    private final List<String> cryptographicBindingMethodsSupported;

    @JsonProperty("cryptographic_suites_supported")
    private final List<String> cryptographicSuitesSupported;

    @JsonSerialize(using = VcTemplateSerializer.class)
    @JsonProperty("credentialSubject")
    private final VcTemplate credentialSubject;
}
