package es.in2.issuer.backend.shared.domain.service;

import reactor.core.publisher.Mono;

public interface IssuerApiClientTokenService {
    Mono<String> getClientToken();
}
