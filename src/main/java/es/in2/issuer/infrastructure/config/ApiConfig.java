package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.ApiProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;

    public ApiConfig(ConfigAdapterFactory configAdapterFactory, ApiProperties apiProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(apiProperties.externalDomain());
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(apiProperties.configSource());
    }

    public long getCacheLifetimeForCredentialOffer() {
        return apiProperties.cacheLifetime().credentialOffer();
    }

    public long getCacheLifetimeForVerifiableCredential() {
        return apiProperties.cacheLifetime().verifiableCredential();
    }

}
