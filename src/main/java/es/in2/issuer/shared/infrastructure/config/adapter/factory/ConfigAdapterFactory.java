package es.in2.issuer.shared.infrastructure.config.adapter.factory;

import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.impl.YamlConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigAdapterFactory {

    private final ApiProperties apiProperties;
    private final AzureConfigAdapter azureConfigAdapter;
    private final YamlConfigAdapter yamlConfigAdapter;

    public ConfigAdapter getAdapter() {
        return switch (apiProperties.configSource()) {
            case "azure" -> azureConfigAdapter;
            case "yaml" -> yamlConfigAdapter;
            default ->
                    throw new IllegalArgumentException("Invalid Config Adapter Provider: " + apiProperties.configSource());
        };
    }

}
