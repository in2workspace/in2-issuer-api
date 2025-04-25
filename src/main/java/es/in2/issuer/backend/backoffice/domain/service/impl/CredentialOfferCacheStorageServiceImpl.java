package es.in2.issuer.backend.backoffice.domain.service.impl;

import es.in2.issuer.backend.backoffice.domain.exception.CustomCredentialOfferNotFoundException;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.backoffice.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.backend.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.backend.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferCacheStorageServiceImpl implements CredentialOfferCacheStorageService {

    private final CacheStore<CredentialOfferData> cacheStore;

    @Override
    public Mono<String> saveCustomCredentialOffer(CredentialOfferData credentialOfferData) {
        return generateCustomNonce().flatMap(nonce -> cacheStore.add(nonce, credentialOfferData));
    }

    @Override
    public Mono<CredentialOfferData> getCustomCredentialOffer(String nonce) {
        return cacheStore.get(nonce)
                .switchIfEmpty(Mono.error(
                        new CustomCredentialOfferNotFoundException(
                                "CustomCredentialOffer not found for nonce: " + nonce))
                )
                .doOnNext(customCredentialOffer ->
                        log.debug("CustomCredentialOffer found for nonce: {}", nonce)
                )
                .flatMap(customCredentialOffer ->
                        cacheStore.delete(nonce).thenReturn(customCredentialOffer)
                );
    }
}
