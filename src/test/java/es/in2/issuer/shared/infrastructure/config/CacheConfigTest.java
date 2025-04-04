package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

    @Mock
    private ApiProperties apiProperties;

    @InjectMocks
    CacheConfig cacheConfig;

    @Test
    void itShouldReturnCacheLifetimeForCredentialOffer() {
        long expectedCredentialOfferCacheLifetime = 3600L;
        when(apiProperties.cacheLifetime())
                .thenReturn(new ApiProperties.MemoryCache(expectedCredentialOfferCacheLifetime, 0L));

        long actualLifetime = cacheConfig.getCacheLifetimeForCredentialOffer();

        assertThat(actualLifetime)
                .isEqualTo(expectedCredentialOfferCacheLifetime);
    }

    @Test
    void itShouldReturnCacheLifetimeForVerifiableCredential() {
        long expectedVerifiableCredentialLifetime = 7200L;
        when(apiProperties.cacheLifetime())
                .thenReturn(new ApiProperties.MemoryCache(0L, expectedVerifiableCredentialLifetime));

        long actualLifetime = cacheConfig.getCacheLifetimeForVerifiableCredential();

        assertThat(actualLifetime)
                .isEqualTo(expectedVerifiableCredentialLifetime);
    }
}