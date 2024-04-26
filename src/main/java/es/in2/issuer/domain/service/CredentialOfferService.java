package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType, Grant grant);
    Mono<String> createCredentialOfferUri(String nonce);
}
