package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.CredentialRequest;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialIssuanceService {
    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String username, CredentialRequest credentialRequest, String token);
}
