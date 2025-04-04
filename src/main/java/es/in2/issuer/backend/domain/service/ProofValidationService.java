package es.in2.issuer.backend.domain.service;

import reactor.core.publisher.Mono;

public interface ProofValidationService {
    Mono<Boolean> isProofValid(String jwtProof, String token);
}
