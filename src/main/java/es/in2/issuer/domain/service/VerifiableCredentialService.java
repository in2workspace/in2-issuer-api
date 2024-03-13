package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CredentialRequest;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialService {
    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String username, CredentialRequest credentialRequest, String token);
    Mono<String> getVerifiableCredential(String credentialId);
    Mono<String> testPayLoad(String username, String token, String subjectDid);
}
