package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CredentialRequest;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

public interface VerifiableCredentialService {
    //Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String username, CredentialRequest credentialRequest, String token);
    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, Map<String, Object> userData, Instant expiration);
}
