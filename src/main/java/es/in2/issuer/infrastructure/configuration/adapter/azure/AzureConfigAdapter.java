package es.in2.issuer.infrastructure.configuration.adapter.azure;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.infrastructure.configuration.adapter.azure.config.properties.AzureProperties;
import es.in2.issuer.infrastructure.configuration.model.ConfigProviderName;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import es.in2.issuer.infrastructure.configuration.util.ConfigSourceName;
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
