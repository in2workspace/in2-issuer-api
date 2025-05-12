package es.in2.issuer.backend.backoffice.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigAudit;
import es.in2.issuer.backend.backoffice.domain.model.entities.SignatureConfigurationAudit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignatureConfigAuditFactory {

    private final ObjectMapper objectMapper;

    public SignatureConfigAudit createFromEntity(SignatureConfigurationAudit entity) {
        JsonNode oldValues = parseJsonOrEmpty(entity.getOldValues());
        JsonNode newValues = parseJsonOrNull(entity.getNewValues());

        return new SignatureConfigAudit(
                entity.getId().toString(),
                entity.getSignatureConfigurationId(),
                entity.getUserEmail(),
                entity.getOrganizationIdentifier(),
                entity.getInstant().toString(),
                oldValues,
                newValues,
                entity.getRationale(),
                entity.isEncrypted()
        );
    }

    private JsonNode parseJsonOrEmpty(String json) {
        try {
            return json != null ? objectMapper.readTree(json) : objectMapper.createObjectNode();
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }

    private JsonNode parseJsonOrNull(String json) {
        try {
            return json != null ? objectMapper.readTree(json) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
