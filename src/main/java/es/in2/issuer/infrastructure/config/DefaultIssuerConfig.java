package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.DefaultIssuerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DefaultIssuerConfig {
    private final ConfigAdapter configAdapter;
    private final DefaultIssuerProperties defaultIssuerProperties;

    public DefaultIssuerConfig(ConfigAdapterFactory configAdapterFactory, DefaultIssuerProperties defaultIssuerProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.defaultIssuerProperties = defaultIssuerProperties;
    }

    public String getCommonName() {
        return configAdapter.getConfiguration(defaultIssuerProperties.commonName());
    }

    public String getCountry() {
        return configAdapter.getConfiguration(defaultIssuerProperties.country());
    }

    public String getEmail() {
        return configAdapter.getConfiguration(defaultIssuerProperties.email());
    }

    public String getOrganizationIdentifier() {
        return configAdapter.getConfiguration(defaultIssuerProperties.organizationIdentifier());
    }

    public String getOrganization() {
        return configAdapter.getConfiguration(defaultIssuerProperties.organization());
    }

    public String getSerialNumber() {
        return configAdapter.getConfiguration(defaultIssuerProperties.serialNumber());
    }
}
