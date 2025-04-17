package es.in2.issuer.backend.domain.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface CertificateService {
    Mono<String> getOrganizationIdFromCertificate(String cert);
}
