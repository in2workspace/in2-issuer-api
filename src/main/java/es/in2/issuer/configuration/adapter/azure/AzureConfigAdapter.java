package es.in2.issuer.configuration.adapter.azure;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.configuration.adapter.azure.config.properties.AzureProperties;
import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import es.in2.issuer.api.config.properties.AppConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderName.AZURE)

public class AzureConfigAdapter implements GenericConfigAdapter {
    private final ConfigurationClient configurationClient;
    private final AzureProperties azureProperties;
    private final AppConfigurationProperties appConfigurationProperties;

    public AzureConfigAdapter(ConfigurationClient configurationClient, AzureProperties azureProperties, AppConfigurationProperties appConfigurationProperties) {
        this.configurationClient = configurationClient;
        this.azureProperties = azureProperties;
        this.appConfigurationProperties = appConfigurationProperties;
    }

    @Override
    public String getConfiguration(String key){
        return getConfigurationValue(key);
    }
    private String getConfigurationValue(String key) {
        return configurationClient.getConfigurationSetting(key, azureProperties.label().global()).getValue();
    }
}
