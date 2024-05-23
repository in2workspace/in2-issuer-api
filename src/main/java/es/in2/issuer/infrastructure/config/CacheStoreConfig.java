package es.in2.issuer.infrastructure.config;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.VerifiableCredentialJWT;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class CacheStoreConfig {

    private final ApiConfig apiConfig;

    @Bean
    public CacheStore<String> cacheStoreDefault() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStore<>(apiConfig.getCacheLifetimeForVerifiableCredential(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CustomCredentialOffer> cacheStoreForCredentialOffer() {
        return new CacheStore<>(apiConfig.getCacheLifetimeForCredentialOffer(), TimeUnit.MINUTES);
    }

}
