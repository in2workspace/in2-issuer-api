package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record SignatureRequestExternal(
        @JsonProperty("credentialID") String credentialID,
        @JsonProperty("signatureQualifier") String signatureQualifier,
        @JsonProperty("documents") List<DocumentExternal> documents
) {

}
