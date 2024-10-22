package es.in2.issuer.application.workflow;

import es.in2.issuer.domain.model.dto.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VerifiableCredentialIssuanceWorkflow {
    Mono<Void> completeIssuanceCredentialProcess(String processId, String type, IssuanceRequest issuanceRequest);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String processId, CredentialRequest credentialRequest, String token);

    Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(String username, BatchCredentialRequest batchCredentialRequest, String token);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);

    // Method for signing deferred credential using remote DSS, currently not in use in DOME profile
    Mono<Void> signDeferredCredential(String unsignedCredential, String userId, UUID credentialId, String token);

    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, AuthServerNonceRequest authServerNonceRequest);
}
