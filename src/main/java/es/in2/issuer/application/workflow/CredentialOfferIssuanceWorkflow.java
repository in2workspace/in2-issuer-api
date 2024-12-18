package es.in2.issuer.application.workflow;

import es.in2.issuer.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.domain.model.dto.PreAuthCodeResponse;
import reactor.core.publisher.Mono;

public interface CredentialOfferIssuanceWorkflow {
    Mono<String> buildCredentialOfferUri(String processId, String transactionCode);
    Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce);
    Mono<PreAuthCodeResponse> getPreAuthorizationCodeFromIam();
}
