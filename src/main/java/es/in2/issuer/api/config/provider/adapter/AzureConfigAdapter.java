package es.in2.issuer.api.config.provider.adapter;

import es.in2.issuer.api.config.provider.ConfigSourceName;
import es.in2.issuer.api.config.provider.GenericConfigAdapter;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = "azure")

public class AzureConfigAdapter implements GenericConfigAdapter {
    @Override
    public String getBaseUrl() {
        return "baseUrl from Azure";
    }

    @Override
    public String getKeycloakDomain() {
        return null;
    }

    @Override
    public String getIssuerDomain() {
        return null;
    }

    @Override
    public String getAuthenticSourcesDomain() {
        return null;
    }

    @Override
    public String getKeyVaultDomain() {
        return null;
    }

    @Override
    public String getRemoteSignatureDomain() {
        return null;
    }

    @Override
    public String getKeycloakDid() {
        return null;
    }

    @Override
    public String getIssuerDid() {
        return null;
    }
}
