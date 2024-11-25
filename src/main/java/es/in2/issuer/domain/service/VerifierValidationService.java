package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface VerifierValidationService {

    Mono<Void> verifyToken(String accessToken);
}
