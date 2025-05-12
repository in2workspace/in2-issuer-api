package es.in2.issuer.backend.backoffice.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.backoffice.domain.model.dtos.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigAudit;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigurationResponse;
import es.in2.issuer.backend.backoffice.domain.model.entities.SignatureConfigurationAudit;
import es.in2.issuer.backend.backoffice.domain.repository.SignatureConfigurationAuditRepository;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationAuditService;
import es.in2.issuer.backend.backoffice.domain.util.factory.SignatureConfigAuditFactory;
import es.in2.issuer.backend.shared.domain.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SignatureConfigurationAuditImpl implements SignatureConfigurationAuditService {
    private final SignatureConfigurationAuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final AccessTokenService accessTokenService;
    private final SignatureConfigAuditFactory factory;


    @Override
    public Mono<Void> saveAudit(SignatureConfigurationResponse oldConfig,
                                CompleteSignatureConfiguration newConfig,
                                String rationale,
                                String userEmail) {
        try {
            String oldJson = objectMapper.writeValueAsString(oldConfig);
            String newJson = newConfig != null
                    ? objectMapper.writeValueAsString(newConfig)
                    : "{}";
            SignatureConfigurationAudit audit = SignatureConfigurationAudit.builder()
                    .signatureConfigurationId(oldConfig.id().toString())
                    .userEmail(userEmail)
                    .organizationIdentifier(oldConfig.organizationIdentifier())
                    .instant(Instant.now())
                    .oldValues(oldJson)
                    .newValues(newJson)
                    .rationale(rationale)
                    .encrypted(false)
                    .build();

            return auditRepository.save(audit).then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing config audit JSON", e));
        }
    }

    @Override
    public Mono<Void> saveDeletionAudit(SignatureConfigurationResponse oldConfig, String rationale, String userEmail) {
        try {
            String oldJson = objectMapper.writeValueAsString(oldConfig);

            SignatureConfigurationAudit audit = SignatureConfigurationAudit.builder()
                    .signatureConfigurationId(oldConfig.id().toString())
                    .userEmail(userEmail)
                    .organizationIdentifier(oldConfig.organizationIdentifier())
                    .instant(Instant.now())
                    .oldValues(oldJson)
                    .rationale(rationale)
                    .encrypted(false)
                    .build();

            return auditRepository.save(audit).then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing old config for deletion audit", e));
        }
    }

    @Override
    public Flux<SignatureConfigAudit> getAllAudits() {
        return auditRepository.findAll()
                .map(factory:: createFromEntity);
    }

    @Override
    public Flux<SignatureConfigAudit> getAuditsByOrganization(String organizationIdentifier) {
        return auditRepository.findAllByOrganizationIdentifier(organizationIdentifier)
                .map(factory::createFromEntity);
    }


}