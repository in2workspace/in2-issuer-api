package es.in2.issuer.application.workflow;

import es.in2.issuer.domain.model.dto.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VerifiableCredentialIssuanceWorkflow {
    Mono<Void> completeWithdrawCredentialProcess(String processId, String type, CredentialData credentialData, String token);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String processId, CredentialRequest credentialRequest, String token);

    Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(String username, BatchCredentialRequest batchCredentialRequest, String token);

    Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);

    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, AuthServerNonceRequest authServerNonceRequest);
}
