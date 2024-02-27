package es.in2.issuer.configuration.adapter.yaml;

import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import es.in2.issuer.api.config.properties.AppConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderName.YAML)
public class YamlConfigAdapter implements GenericConfigAdapter {
    private final AppConfigurationProperties appConfigurationProperties;

    public YamlConfigAdapter(AppConfigurationProperties appConfigurationProperties) {
        this.appConfigurationProperties = appConfigurationProperties;
    }
    @Override
    public String getConfiguration(String key){
        return key;
    }
}
