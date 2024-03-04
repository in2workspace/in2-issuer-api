package es.in2.issuer.configuration.adapter.azure;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.configuration.adapter.azure.config.properties.AzureProperties;
import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderName.AZURE)

public class AzureConfigAdapter implements GenericConfigAdapter {
    private final ConfigurationClient configurationClient;
    private final AzureProperties azureProperties;

    public AzureConfigAdapter(ConfigurationClient configurationClient, AzureProperties azureProperties) {
        this.configurationClient = configurationClient;
        this.azureProperties = azureProperties;
    }

    @Override
    public String getConfiguration(String key){
        return getConfigurationValue(key);
    }
    private String getConfigurationValue(String key) {
        return configurationClient.getConfigurationSetting(key, azureProperties.label().global()).getValue();
    }
}
