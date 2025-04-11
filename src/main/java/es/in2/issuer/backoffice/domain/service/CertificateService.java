package es.in2.issuer.backoffice.domain.service;

import reactor.core.publisher.Mono;

public interface CertificateService {
    Mono<String> getOrganizationIdFromCertificate(String cert);
}
