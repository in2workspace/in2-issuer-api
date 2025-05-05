package es.in2.issuer.backend.shared.domain.service;

import reactor.core.publisher.Mono;

public interface NonceValidationService {
    Mono<Boolean> isValid(String processId, Mono<String> nonceMono);
}
