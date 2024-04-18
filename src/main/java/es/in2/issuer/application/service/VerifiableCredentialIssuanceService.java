package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.*;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialIssuanceService {
    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String username, CredentialRequest credentialRequest, String token);
    Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(String username, BatchCredentialRequest batchCredentialRequest, String token);
    Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String userId, DeferredCredentialRequest deferredCredentialRequest, String token);

}
