package es.in2.issuer.backend.shared.infrastructure.config.adapter.impl;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.backend.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.shared.infrastructure.config.properties.AzureProperties;
import org.springframework.stereotype.Component;

@Component
public class AzureConfigAdapter implements ConfigAdapter {

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
