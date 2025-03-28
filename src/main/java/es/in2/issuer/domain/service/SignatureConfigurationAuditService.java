package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CompleteSignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureConfigAudit;
import es.in2.issuer.domain.model.entities.SignatureConfigurationAudit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SignatureConfigurationAuditService {
    Mono<Void> saveAudit(CompleteSignatureConfiguration oldConfig, CompleteSignatureConfiguration newConfig, String rationale, String userEmail);
    Mono<Void> saveDeletionAudit(CompleteSignatureConfiguration oldConfig, String rationale, String userEmail);

    Flux<SignatureConfigAudit> getAllAudits();
    Flux<SignatureConfigAudit> getAuditsByOrganization(String organizationIdentifier);
}
