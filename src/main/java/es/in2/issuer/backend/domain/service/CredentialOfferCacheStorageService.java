package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import reactor.core.publisher.Mono;

public interface CredentialOfferCacheStorageService {

    Mono<String> saveCustomCredentialOffer(CredentialOfferData credentialOfferData);

    Mono<CredentialOfferData> getCustomCredentialOffer(String nonce);

}
