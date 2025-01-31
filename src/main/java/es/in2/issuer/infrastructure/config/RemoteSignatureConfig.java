package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.RemoteSignatureProperties;
import org.springframework.stereotype.Component;

@Component
public class RemoteSignatureConfig {

    private final ConfigAdapter configAdapter;
    private final RemoteSignatureProperties remoteSignatureProperties;

    public RemoteSignatureConfig(ConfigAdapterFactory configAdapterFactory, RemoteSignatureProperties remoteSignatureProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.remoteSignatureProperties = remoteSignatureProperties;
    }

    public String getRemoteSignatureDomain() {
        return configAdapter.getConfiguration(remoteSignatureProperties.domain());
    }

    public String getRemoteSignatureSignPath() {
        return configAdapter.getConfiguration(remoteSignatureProperties.paths().signPath());
    }
    
}
