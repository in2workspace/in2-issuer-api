package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType, String preAuthCode);
    Mono<String> createCredentialOfferUri(String nonce);
}
