package es.in2.issuer.infrastructure.config;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.VerifiableCredentialJWT;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class IssuerApplicationConfig {
    @Bean
    public CacheStore<String> cacheStoreForString() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<VerifiableCredentialJWT> cacheStoreForVerifiableCredentialJwt() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheStore<CustomCredentialOffer> cacheStoreForCredentialOffer() {
        return new CacheStore<>(10, TimeUnit.MINUTES);
    }

}
