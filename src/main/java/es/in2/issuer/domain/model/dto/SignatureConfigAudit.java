package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record SignatureConfigAudit(String id,
                                   String signatureConfigurationId,
                                   String userEmail,
                                   String organizationIdentifier,
                                   String instant,
                                   JsonNode  oldValues,
                                   JsonNode newValues,
                                   String rationale,
                                   boolean encrypted) {
}
