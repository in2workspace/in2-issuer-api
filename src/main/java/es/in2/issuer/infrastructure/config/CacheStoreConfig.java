package es.in2.issuer.infrastructure.config;

import es.in2.issuer.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class CacheStoreConfig {

    private final AppConfig appConfig;

    @Bean
    public CacheStore<String> cacheStoreDefault() {
        return new CacheStore<>(72, TimeUnit.HOURS);
    }

    @Bean
    public CacheStore<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStore<>(appConfig.getCacheLifetimeForVerifiableCredential(), TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CustomCredentialOffer> cacheStoreForCredentialOffer() {
        return new CacheStore<>(appConfig.getCacheLifetimeForCredentialOffer(), TimeUnit.MINUTES);
    }

}
