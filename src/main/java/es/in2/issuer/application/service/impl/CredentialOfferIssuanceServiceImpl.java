package es.in2.issuer.application.service.impl;

import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.exception.CredentialTypeUnsuportedException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.domain.service.VcSchemaService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceServiceImpl implements CredentialOfferIssuanceService {

    private final VcSchemaService vcSchemaService;
    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;

    @Override
    public Mono<String> buildCredentialOfferUri(String credentialType) {
        return vcSchemaService.isSupportedVcSchema(credentialType).flatMap(isSupported -> {
            if (!isSupported) {
                return Mono.error(new CredentialTypeUnsuportedException(credentialType));
            }
            return credentialOfferService.buildCustomCredentialOffer(credentialType)
                    .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                    .flatMap(credentialOfferService::createCredentialOfferUri);
        });
    }

    @Override
    public Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce) {
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce);
    }

}
