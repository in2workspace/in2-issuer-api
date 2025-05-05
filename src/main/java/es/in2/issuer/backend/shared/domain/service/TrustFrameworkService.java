package es.in2.issuer.backend.shared.domain.service;

import reactor.core.publisher.Mono;

public interface TrustFrameworkService {
    Mono<Void> registerDid(String processId, String did);
    Mono<Boolean> validateDidFormat(String processId, String did);
}
