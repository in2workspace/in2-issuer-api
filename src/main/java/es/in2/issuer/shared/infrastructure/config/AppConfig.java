package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;

    public AppConfig(
            ConfigAdapterFactory configAdapterFactory,
            ApiProperties apiProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(apiProperties.externalDomain());
    }
}
