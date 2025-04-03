package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.shared.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static es.in2.issuer.shared.domain.util.Constants.TX_CODE_BY_PRE_AUTH_CODE_CACHE_STORAGE_EXPIRY_DURATION_MINUTES;

@Configuration
@RequiredArgsConstructor
public class CacheStoreConfig {

    private final CacheConfig cacheConfig;

    @Bean
    public CacheStoreRepository<String> cacheStoreDefault() {
        return new CacheStoreRepository<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStoreRepository<String> cacheStoreForTransactionCode() {
        return new CacheStoreRepository<>(72, TimeUnit.HOURS);
    }
    @Bean
    public CacheStoreRepository<String> cacheStoreForCTransactionCode() {
        return new CacheStoreRepository<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStoreRepository<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStoreRepository<>(cacheConfig.getCacheLifetimeForVerifiableCredential(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStoreRepository<CredentialOfferData> cacheStoreForCredentialOffer() {
        return new CacheStoreRepository<>(cacheConfig.getCacheLifetimeForCredentialOffer(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStoreRepository<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore() {
        return new CacheStoreRepository<>(TX_CODE_BY_PRE_AUTH_CODE_CACHE_STORAGE_EXPIRY_DURATION_MINUTES, TimeUnit.MINUTES);
    }
}
