package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.backoffice.domain.model.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.SignatureConfigAudit;
import es.in2.issuer.backend.backoffice.domain.model.SignatureConfigurationResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SignatureConfigurationAuditService {
    Mono<Void> saveAudit(SignatureConfigurationResponse oldConfig, CompleteSignatureConfiguration newConfig, String rationale, String userEmail);
    Mono<Void> saveDeletionAudit(SignatureConfigurationResponse oldConfig, String rationale, String userEmail);

    Flux<SignatureConfigAudit> getAllAudits();
    Flux<SignatureConfigAudit> getAuditsByOrganization(String organizationIdentifier);
}
