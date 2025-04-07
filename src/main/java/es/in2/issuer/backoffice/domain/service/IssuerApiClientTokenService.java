package es.in2.issuer.backoffice.domain.service;

import reactor.core.publisher.Mono;

public interface IssuerApiClientTokenService {
    Mono<String> getClientToken();
}
