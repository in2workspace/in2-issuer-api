package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {
    private final ApiProperties apiProperties;

    public long getCacheLifetimeForCredentialOffer() {
        return apiProperties.cacheLifetime().credentialOffer();
    }

    public long getCacheLifetimeForVerifiableCredential() {
        return apiProperties.cacheLifetime().verifiableCredential();
    }
}
