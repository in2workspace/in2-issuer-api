package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

import java.time.Instant;

public interface VerifiableCredentialService {
    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
    Mono<String> generateDeferredVcPayLoad(String vc);
    Mono<String> generateVc(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
}
