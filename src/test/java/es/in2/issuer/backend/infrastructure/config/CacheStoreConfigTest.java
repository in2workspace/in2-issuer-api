package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.backend.infrastructure.config.CacheStoreConfig;
import es.in2.issuer.backend.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheStoreConfigTest {

    @Mock
    private AppConfig appConfig;

    private CacheStoreConfig cacheStoreConfig;

    @BeforeEach
    void setUp() {
        cacheStoreConfig = new CacheStoreConfig(appConfig);
    }

    @Test
    void testCacheStoreDefault() {
        CacheStore<String> stringCacheStore = cacheStoreConfig.cacheStoreDefault();
        assertNotNull(stringCacheStore);
    }

    @Test
    void testCacheStoreForTransactionCode() {
        CacheStore<String> cacheStoreForTransactionCode = cacheStoreConfig.cacheStoreForTransactionCode();
        assertNotNull(cacheStoreForTransactionCode);
    }

    @Test
    void testCacheStoreForVerifiableCredentialJwt() {
        long cacheLifetime = 30;
        when(appConfig.getCacheLifetimeForVerifiableCredential()).thenReturn(cacheLifetime);

        CacheStore<VerifiableCredentialJWT> verifiableCredentialJWTCacheStore = cacheStoreConfig.cacheStoreForVerifiableCredentialJwt();
        assertNotNull(verifiableCredentialJWTCacheStore);
    }

    @Test
    void testCacheStoreForCredentialOffer() {
        long cacheLifetime = 60;
        when(appConfig.getCacheLifetimeForCredentialOffer()).thenReturn(cacheLifetime);

        CacheStore<CredentialOfferData> customCredentialOfferCacheStore = cacheStoreConfig.cacheStoreForCredentialOffer();
        assertNotNull(customCredentialOfferCacheStore);
    }
}