package es.in2.issuer.api.config.provider.adapter;

import es.in2.issuer.api.config.provider.ConfigSourceName;
import es.in2.issuer.api.config.provider.GenericConfigAdapter;
import es.in2.issuer.api.config.provider.properties.SecretProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = "yaml")
public class YamlConfigAdapter implements GenericConfigAdapter {
    private final SecretProperties secretProperties;

    @Autowired
    public YamlConfigAdapter(SecretProperties secretProperties) {
        this.secretProperties = secretProperties;
    }

    @Override
    public String getBaseUrl() {
        return "http://localhost:8080";
    }

    @Override
    public String getKeycloakDomain() {
        return secretProperties.keycloakDomain();
    }

    @Override
    public String getIssuerDomain() {
        return secretProperties.issuerDomain();
    }

    @Override
    public String getAuthenticSourcesDomain() {
        return secretProperties.authenticSourcesDomain();
    }

    @Override
    public String getKeyVaultDomain() {
        return secretProperties.keyVaultDomain();
    }

    @Override
    public String getRemoteSignatureDomain() {
        return secretProperties.remoteSignatureDomain();
    }

    @Override
    public String getKeycloakDid() {
        return secretProperties.keycloakDid();
    }

    @Override
    public String getIssuerDid() {
        return secretProperties.issuerDid();
    }
}
