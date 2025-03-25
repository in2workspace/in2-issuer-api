package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.domain.model.dto.IssuanceRequest;
import es.in2.issuer.domain.model.dto.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialService {
    Mono<String> generateVc(String processId, String vcType, IssuanceRequest issuanceRequest, String token);
    Mono<String> generateVerifiableCertification(String processId, IssuanceRequest issuanceRequest, String idToken);
    Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format, String token, String operationMode);
    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode);
    Mono<VerifiableCredentialResponse> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);
}
