package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.VcTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CredentialSchemaService {
    Mono<Boolean> isSupportedVcSchema(String credentialType);
    Mono<List<VcTemplate>> getAllVcTemplates();
    Mono<List<VcTemplate>> getAllDetailedVcTemplates();
    Mono<VcTemplate> getTemplate(String templateName);
}
