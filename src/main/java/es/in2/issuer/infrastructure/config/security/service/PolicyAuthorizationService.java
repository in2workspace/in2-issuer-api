package es.in2.issuer.infrastructure.config.security.service;

import reactor.core.publisher.Mono;

public interface PolicyAuthorizationService {
    Mono<Void> authorize(String authorizationHeader, String schema);
}
