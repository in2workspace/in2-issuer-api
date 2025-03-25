package es.in2.issuer.backend.application.workflow;

import es.in2.issuer.backend.domain.model.dto.*;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialIssuanceWorkflow {
    Mono<Void> completeIssuanceCredentialProcess(String processId, String type, IssuanceRequest issuanceRequest, String token);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String processId, CredentialRequest credentialRequest, String token);

    Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(String username, BatchCredentialRequest batchCredentialRequest, String token);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);

    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, AuthServerNonceRequest authServerNonceRequest);
}
