package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.backend.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static es.in2.issuer.backend.domain.util.Constants.CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME;
import static es.in2.issuer.backend.domain.util.Constants.VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME;

@Configuration
@RequiredArgsConstructor
public class CacheStoreConfig {

    @Bean
    public CacheStore<String> cacheStoreDefault() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<String> cacheStoreForTransactionCode() {
        return new CacheStore<>(72, TimeUnit.HOURS);
    }
    @Bean
    public CacheStore<String> cacheStoreForCTransactionCode() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStore<>(VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CredentialOfferData> cacheStoreForCredentialOffer() {
        return new CacheStore<>(CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

}
