package es.in2.issuer.domain.service;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface CertificateService {
    Mono<String> getOrganizationIdFromCertificate(ServerWebExchange exchange);
}
