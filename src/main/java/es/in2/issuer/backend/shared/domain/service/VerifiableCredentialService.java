package es.in2.issuer.backend.shared.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedDataCredential;
import es.in2.issuer.backend.shared.domain.model.dto.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialService {
    Mono<String> generateVc(String processId, String vcType, PreSubmittedDataCredential preSubmittedDataCredential, String token);
    Mono<String> generateVerifiableCertification(String processId, PreSubmittedDataCredential preSubmittedDataCredential, String idToken);
    Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format, String token);
    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode);
    Mono<VerifiableCredentialResponse> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);
}
