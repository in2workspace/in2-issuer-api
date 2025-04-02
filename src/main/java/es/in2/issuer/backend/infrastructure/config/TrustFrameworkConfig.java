package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.TrustFrameworkProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TrustFrameworkConfig {

    private final ConfigAdapter configAdapter;
    private final TrustFrameworkProperties trustFrameworkProperties;

    public TrustFrameworkConfig(ConfigAdapterFactory configAdapterFactory, TrustFrameworkProperties trustFrameworkProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.trustFrameworkProperties = trustFrameworkProperties;
    }

    public String getTrustFrameworkUrl() {
        return configAdapter.getConfiguration(trustFrameworkProperties.url());
    }

}
