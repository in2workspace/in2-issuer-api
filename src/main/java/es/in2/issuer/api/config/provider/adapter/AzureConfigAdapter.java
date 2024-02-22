package es.in2.issuer.api.config.provider.adapter;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.api.config.provider.properties.AzureProperties;
import es.in2.issuer.api.config.provider.ConfigProviderNameEnum;
import es.in2.issuer.api.config.provider.ConfigSourceName;
import es.in2.issuer.api.config.provider.GenericConfigAdapter;
import es.in2.issuer.api.config.provider.properties.SecretProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderNameEnum.AZURE)

public class AzureConfigAdapter implements GenericConfigAdapter {
    private final ConfigurationClient configurationClient;
    private final AzureProperties azureProperties;
    private final SecretProperties secretProperties;

    public AzureConfigAdapter(ConfigurationClient configurationClient, AzureProperties azureProperties, SecretProperties secretProperties) {
        this.configurationClient = configurationClient;
        this.azureProperties = azureProperties;
        this.secretProperties = secretProperties;
    }

    @Override
    public String getKeycloakDomain() {
        return getConfigurationValue(secretProperties.keycloakDomain());
    }

    @Override
    public String getIssuerDomain() {
        return getConfigurationValue(secretProperties.issuerDomain());
    }

    @Override
    public String getAuthenticSourcesDomain() {
        return getConfigurationValue(secretProperties.authenticSourcesDomain());
    }

    @Override
    public String getKeyVaultDomain() {
        return getConfigurationValue(secretProperties.keyVaultDomain());
    }

    @Override
    public String getRemoteSignatureDomain() {
        return getConfigurationValue(secretProperties.remoteSignatureDomain());
    }

    @Override
    public String getKeycloakDid() {
        return getConfigurationValue(secretProperties.keycloakDid());
    }

    @Override
    public String getIssuerDid() {
        return getConfigurationValue(secretProperties.issuerDid());
    }

    private String getConfigurationValue(String key) {
        return configurationClient.getConfigurationSetting(key, azureProperties.label().global()).getValue();
    }
}
