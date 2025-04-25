package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.CredentialOfferData;
import reactor.core.publisher.Mono;

public interface CredentialOfferCacheStorageService {

    Mono<String> saveCustomCredentialOffer(CredentialOfferData credentialOfferData);

    Mono<CredentialOfferData> getCustomCredentialOffer(String nonce);

}
