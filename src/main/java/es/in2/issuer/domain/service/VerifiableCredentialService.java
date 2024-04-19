package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

public interface VerifiableCredentialService {
    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
}
