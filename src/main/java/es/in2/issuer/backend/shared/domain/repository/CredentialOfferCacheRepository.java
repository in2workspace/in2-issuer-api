package es.in2.issuer.backend.shared.domain.repository;

import es.in2.issuer.backend.shared.domain.model.dto.CredentialOfferData;
import reactor.core.publisher.Mono;

public interface CredentialOfferCacheRepository {
    Mono<String> saveCustomCredentialOffer(CredentialOfferData credentialOfferData);

    Mono<CredentialOfferData> findCredentialOfferById(String id);
}
