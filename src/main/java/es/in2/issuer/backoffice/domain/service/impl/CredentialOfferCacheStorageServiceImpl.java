package es.in2.issuer.backoffice.domain.service.impl;

import es.in2.issuer.backoffice.domain.exception.CustomCredentialOfferNotFoundException;
import es.in2.issuer.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backoffice.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferCacheStorageServiceImpl implements CredentialOfferCacheStorageService {

    private final CacheStoreRepository<CredentialOfferData> cacheStoreRepository;

    @Override
    public Mono<String> saveCustomCredentialOffer(CredentialOfferData credentialOfferData) {
        return generateCustomNonce().flatMap(nonce -> cacheStoreRepository.add(nonce, credentialOfferData));
    }

    @Override
    public Mono<CredentialOfferData> getCustomCredentialOffer(String nonce) {
        return cacheStoreRepository.get(nonce)
                .switchIfEmpty(Mono.error(
                        new CustomCredentialOfferNotFoundException(
                                "CustomCredentialOffer not found for nonce: " + nonce))
                )
                .doOnNext(customCredentialOffer ->
                        log.debug("CustomCredentialOffer found for nonce: {}", nonce)
                )
                .flatMap(customCredentialOffer ->
                        cacheStoreRepository.delete(nonce).thenReturn(customCredentialOffer)
                );
    }
}
