package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.DefaultSignerProperties;
import es.in2.issuer.infrastructure.config.properties.RemoteSignatureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DefaultSignerConfig {
    private final ConfigAdapter configAdapter;
    private final DefaultSignerProperties defaultSignerProperties;
    private final RemoteSignatureProperties remoteSignatureProperties;

    public DefaultSignerConfig(ConfigAdapterFactory configAdapterFactory, DefaultSignerProperties defaultSignerProperties, RemoteSignatureProperties remoteSignatureProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.defaultSignerProperties = defaultSignerProperties;
        this.remoteSignatureProperties = remoteSignatureProperties;
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
        if (configAdapter.getConfiguration(remoteSignatureProperties.type()).equals("server")) {
            return configAdapter.getConfiguration(defaultSignerProperties.organizationIdentifier());
        } else {
            return "VATES-D70795026";
        }
    }

    public String getOrganization() {
        return configAdapter.getConfiguration(defaultSignerProperties.organization());
    }

    public String getSerialNumber() {
        return configAdapter.getConfiguration(defaultSignerProperties.serialNumber());
    }
}
