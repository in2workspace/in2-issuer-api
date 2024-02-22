package es.in2.issuer.api.config.provider;

import org.springframework.stereotype.Service;

@Service
public class ConfigProviderImpl implements ConfigProvider {
    private final GenericConfigAdapter genericConfigAdapter;

    public ConfigProviderImpl(ConfigAdapterFactory configAdapterFactory){
        this.genericConfigAdapter = configAdapterFactory.getAdapter();
    }

    @Override
    public String getKeycloakDomain() {
        return genericConfigAdapter.getKeycloakDomain();
    }

    @Override
    public String getIssuerDomain() {
        return genericConfigAdapter.getIssuerDomain();
    }

    @Override
    public String getAuthenticSourcesDomain() {
        return genericConfigAdapter.getAuthenticSourcesDomain();
    }

    @Override
    public String getKeyVaultDomain() {
        return genericConfigAdapter.getKeyVaultDomain();
    }

    @Override
    public String getRemoteSignatureDomain() {
        return genericConfigAdapter.getRemoteSignatureDomain();
    }

    @Override
    public String getKeycloakDid() {
        return genericConfigAdapter.getKeycloakDid();
    }

    @Override
    public String getIssuerDid() {
        return genericConfigAdapter.getIssuerDid();
    }
}
