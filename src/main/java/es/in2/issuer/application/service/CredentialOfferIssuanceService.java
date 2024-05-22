package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import reactor.core.publisher.Mono;

public interface CredentialOfferIssuanceService {
    Mono<String> buildCredentialOfferUri(String processId, String transactionCode);
    Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce);
}
