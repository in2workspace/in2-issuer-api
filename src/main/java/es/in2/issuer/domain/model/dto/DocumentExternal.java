package es.in2.issuer.domain.model.dto;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;

@Builder

public record DocumentExternal(
        @JsonProperty("document")
        String document,
        @JsonProperty("signature_format")
        String signatureFormat,
        @JsonProperty("conformance_level")
        String conformanceLevel,
        @JsonProperty("signAlgo")
        String signAlgo
) {
}
