package es.in2.issuer.backend.shared.infrastructure.config;

import es.in2.issuer.backend.shared.infrastructure.config.properties.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static es.in2.issuer.backend.shared.domain.util.Constants.CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME;
import static es.in2.issuer.backend.shared.domain.util.Constants.VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CacheConfigTest {

    @Mock
    private AppProperties appProperties;

    @InjectMocks
    CacheConfig cacheConfig;

    @Test
    void itShouldReturnCacheLifetimeForCredentialOffer() {
        long expectedCredentialOfferCacheLifetime = CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME;

        long actualLifetime = cacheConfig.getCacheLifetimeForCredentialOffer();

        assertThat(actualLifetime)
                .isEqualTo(expectedCredentialOfferCacheLifetime);
    }

    @Test
    void itShouldReturnCacheLifetimeForVerifiableCredential() {
        long expectedVerifiableCredentialLifetime = VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME;

        long actualLifetime = cacheConfig.getCacheLifetimeForVerifiableCredential();

        assertThat(actualLifetime)
                .isEqualTo(expectedVerifiableCredentialLifetime);
    }
}