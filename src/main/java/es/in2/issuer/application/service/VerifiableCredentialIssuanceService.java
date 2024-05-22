package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VerifiableCredentialIssuanceService {
    Mono<Void> completeWithdrawLearCredentialProcess(String processId, LEARCredentialRequest learCredentialRequest);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String username, CredentialRequest credentialRequest, String token);
    Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(String username, BatchCredentialRequest batchCredentialRequest, String token);
    Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);
    // Method for signing deferred credential using remote DSS, currently not in use in DOME profile
    Mono<Void> signDeferredCredential(String unsignedCredential, String userId, UUID credentialId, String token);
    // Method for generating and signing credential using remote DSS, currently not in use in DOME profile
    Mono<String> signCredentialOnRequestedFormat(String unsignedCredential, String format, String userId, UUID credentialId, String token);

}
