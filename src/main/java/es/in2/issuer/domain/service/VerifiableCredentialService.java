package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialRequest;
import es.in2.issuer.domain.model.dto.VerifiableCredentialResponse;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialService {
    Mono<String> generateVc(String processId, String vcType, LEARCredentialRequest learCredentialRequest);
//    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
    Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format);

    Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode);
    Mono<VerifiableCredentialResponse> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest);
}
