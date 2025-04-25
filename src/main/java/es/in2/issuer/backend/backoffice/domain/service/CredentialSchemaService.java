package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.backoffice.domain.model.dto.VcTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CredentialSchemaService {
    Mono<Boolean> isSupportedVcSchema(String credentialType);

    Mono<List<VcTemplate>> getAllVcTemplates();

    Mono<List<VcTemplate>> getAllDetailedVcTemplates();

    Mono<VcTemplate> getTemplate(String templateName);
}
