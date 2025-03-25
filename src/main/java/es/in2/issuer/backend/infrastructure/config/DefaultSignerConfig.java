package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.DefaultSignerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DefaultSignerConfig {
    private final ConfigAdapter configAdapter;
    private final DefaultSignerProperties defaultSignerProperties;

    public DefaultSignerConfig(ConfigAdapterFactory configAdapterFactory, DefaultSignerProperties defaultSignerProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.defaultSignerProperties = defaultSignerProperties;
    }

    public String getCommonName() {
        return configAdapter.getConfiguration(defaultSignerProperties.commonName());
    }

    public String getCountry() {
        return configAdapter.getConfiguration(defaultSignerProperties.country());
    }

    public String getEmail() {
        return configAdapter.getConfiguration(defaultSignerProperties.email());
    }

    public String getOrganizationIdentifier() {
        return configAdapter.getConfiguration(defaultSignerProperties.organizationIdentifier());
    }

    public String getOrganization() {
        return configAdapter.getConfiguration(defaultSignerProperties.organization());
    }

    public String getSerialNumber() {
        return configAdapter.getConfiguration(defaultSignerProperties.serialNumber());
    }
}
