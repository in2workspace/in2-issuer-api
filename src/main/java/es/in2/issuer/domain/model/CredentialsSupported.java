package es.in2.issuer.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;

import java.util.List;

@Builder
public record CredentialsSupported(@JsonProperty("format") String format, @JsonProperty("id") String id,
                                   @JsonProperty("types") List<String> types,
                                   @JsonProperty("cryptographic_binding_methods_supported") List<String> cryptographicBindingMethodsSupported,
                                   @JsonProperty("cryptographic_suites_supported") List<String> cryptographicSuitesSupported,
                                   @JsonSerialize(using = VcTemplateSerializer.class) @JsonProperty("credentialSubject") VcTemplate credentialSubject) {
}
