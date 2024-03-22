package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import reactor.core.publisher.Mono;

public interface CredentialOfferCacheStorageService {

    Mono<String> saveCustomCredentialOffer(CustomCredentialOffer customCredentialOffer);

    Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce);

}
