package es.in2.issuer.backend.backoffice.application.workflow;

import es.in2.issuer.backend.backoffice.domain.model.CredentialOfferUriResponse;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialOffer;
import reactor.core.publisher.Mono;

public interface ActivationCodeWorkflow {
    Mono<CredentialOfferUriResponse> buildCredentialOfferUri(String processId, String transactionCode);
    Mono<CredentialOfferUriResponse> buildNewCredentialOfferUri(String processId, String cTransactionCode);
}
