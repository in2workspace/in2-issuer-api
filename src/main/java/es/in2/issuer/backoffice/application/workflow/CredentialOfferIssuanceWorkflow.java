package es.in2.issuer.backoffice.application.workflow;

import es.in2.issuer.backoffice.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import reactor.core.publisher.Mono;

public interface CredentialOfferIssuanceWorkflow {
    Mono<CredentialOfferUriResponse> buildCredentialOfferUri(String processId, String transactionCode);
    Mono<CredentialOfferUriResponse> buildNewCredentialOfferUri(String processId, String cTransactionCode);
    Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce);
}
