package es.in2.issuer.backend.domain.service.impl;

import es.in2.issuer.backend.domain.model.dto.VcTemplate;
import es.in2.issuer.backend.domain.service.CredentialSchemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

// fixme: esta clase la debemos eliminar y buscar un sistema de configuración para los templates, actualmente esta devolviendo data dummy
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialSchemaServiceImpl implements CredentialSchemaService {

    private final List<String> vcTemplateNames = Arrays.asList(
            "LEARCredentialEmployee"
    );

    @Override
    public Mono<Boolean> isSupportedVcSchema(String credentialType) {
        return Mono.fromCallable(() -> vcTemplateNames.contains(credentialType));
    }

    @Override
    public Mono<List<VcTemplate>> getAllVcTemplates() {
        return Flux.fromIterable(vcTemplateNames)
                .concatMap(vc -> Mono.fromCallable(() -> new VcTemplate(false, vc, null)))
                .collectList();
    }

    @Override
    public Mono<List<VcTemplate>> getAllDetailedVcTemplates() {
        return Flux.fromIterable(vcTemplateNames)
                .concatMap(name -> Mono.fromCallable(() -> new VcTemplate(false, name, null)))
                .collectList();
    }

    @Override
    public Mono<VcTemplate> getTemplate(String templateName) {
        return Mono.fromCallable(() -> new VcTemplate(false, templateName, null))
                .onErrorMap(IllegalArgumentException.class, e -> new IllegalArgumentException("Error getting template: " + e.getMessage(), e));
    }

}
