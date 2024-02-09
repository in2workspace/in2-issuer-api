package es.in2.issuer.api.service;

import id.walt.credentials.w3c.templates.VcTemplate;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IssuerVcTemplateService {
    Mono<List<VcTemplate>> getAllVcTemplates();
    Mono<List<VcTemplate>> getAllDetailedVcTemplates();
    Mono<VcTemplate> getTemplate(String templateName);
}
