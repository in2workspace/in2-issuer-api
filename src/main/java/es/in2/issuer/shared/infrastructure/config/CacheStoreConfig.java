package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static es.in2.issuer.authserver.domain.utils.Constants.TX_CODE_BY_PRE_AUTH_CODE_CACHE_STORAGE_EXPIRY_DURATION_MINUTES;

@Configuration
@RequiredArgsConstructor
public class CacheStoreConfig {

    private final AppConfig appConfig;

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
        return new CacheStore<>(appConfig.getCacheLifetimeForVerifiableCredential(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CredentialOfferData> cacheStoreForCredentialOffer() {
        return new CacheStore<>(appConfig.getCacheLifetimeForCredentialOffer(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore() {
        return new CacheStore<>(TX_CODE_BY_PRE_AUTH_CODE_CACHE_STORAGE_EXPIRY_DURATION_MINUTES, TimeUnit.MINUTES);
    }
}
