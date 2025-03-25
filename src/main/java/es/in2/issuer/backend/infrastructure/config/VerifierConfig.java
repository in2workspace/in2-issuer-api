package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.VerifierProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerifierConfig {
    private final ConfigAdapter configAdapter;
    private final VerifierProperties verifierProperties;

    public VerifierConfig(ConfigAdapterFactory configAdapterFactory, VerifierProperties verifierProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.verifierProperties = verifierProperties;
    }

    public String getVerifierExternalDomain() {
        return configAdapter.getConfiguration(verifierProperties.externalDomain());
    }

}
