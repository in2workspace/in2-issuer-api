package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface ProofValidationService {
    Mono<Boolean> isProofValid(String jwtProof);
    Mono<String> deleteNonceAndGenerateAFreshOne(String oldNonce);
}
