package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.config.CacheConfig;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.shared.domain.model.dto.VerifiableCredentialJWT;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheStoreRepositoryConfigTest {

    @Mock
    private CacheConfig cacheConfig;

    private CacheStoreConfig cacheStoreConfig;

    @BeforeEach
    void setUp() {
        cacheStoreConfig = new CacheStoreConfig(cacheConfig);
    }

    @Test
    void testCacheStoreDefault() {
        CacheStoreRepository<String> stringCacheStoreRepository = cacheStoreConfig.cacheStoreDefault();
        assertNotNull(stringCacheStoreRepository);
    }

    @Test
    void testCacheStoreForTransactionCode() {
        CacheStoreRepository<String> cacheStoreRepositoryForTransactionCode = cacheStoreConfig.cacheStoreForTransactionCode();
        assertNotNull(cacheStoreRepositoryForTransactionCode);
    }

    @Test
    void testCacheStoreForVerifiableCredentialJwt() {
        long cacheLifetime = 30;
        when(cacheConfig.getCacheLifetimeForVerifiableCredential()).thenReturn(cacheLifetime);

        CacheStoreRepository<VerifiableCredentialJWT> verifiableCredentialJWTCacheStoreRepository = cacheStoreConfig.cacheStoreForVerifiableCredentialJwt();
        assertNotNull(verifiableCredentialJWTCacheStoreRepository);
    }

    @Test
    void testCacheStoreForCredentialOffer() {
        long cacheLifetime = 60;
        when(cacheConfig.getCacheLifetimeForCredentialOffer()).thenReturn(cacheLifetime);

        CacheStoreRepository<CredentialOfferData> customCredentialOfferCacheStoreRepository = cacheStoreConfig.cacheStoreForCredentialOffer();
        assertNotNull(customCredentialOfferCacheStoreRepository);
    }

    @Test
    void testCacheStoreForCredentialIdAndTxCodeByPreAuthorizedCodeCacheStore() {
        CacheStoreRepository<CredentialIdAndTxCode> customCredentialOfferCacheStoreRepository =
                cacheStoreConfig.credentialIdAndTxCodeByPreAuthorizedCodeCacheStore();
        assertNotNull(customCredentialOfferCacheStoreRepository);
    }
}