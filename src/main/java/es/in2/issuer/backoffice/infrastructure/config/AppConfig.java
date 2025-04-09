package es.in2.issuer.backoffice.infrastructure.config;

import es.in2.issuer.backoffice.infrastructure.config.properties.CorsProperties;
import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final CorsProperties corsProperties;

    public AppConfig(
                        ConfigAdapterFactory configAdapterFactory,
                        ApiProperties apiProperties,
                        CorsProperties corsProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.corsProperties = corsProperties;
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(apiProperties.configSource());
    }

    public List<String> getExternalCorsAllowedOrigins() {
        return corsProperties.externalAllowedOrigins();
    }
    public List<String> getDefaultCorsAllowedOrigins() {
        return corsProperties.defaultAllowedOrigins();
    }

}
