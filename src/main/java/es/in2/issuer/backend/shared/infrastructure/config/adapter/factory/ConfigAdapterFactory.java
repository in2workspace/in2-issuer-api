package es.in2.issuer.backend.shared.infrastructure.config.adapter.factory;

import es.in2.issuer.backend.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.shared.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.backend.shared.infrastructure.config.adapter.impl.YamlConfigAdapter;
import es.in2.issuer.backend.shared.infrastructure.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigAdapterFactory {

    private final AppProperties appProperties;
    private final AzureConfigAdapter azureConfigAdapter;
    private final YamlConfigAdapter yamlConfigAdapter;

    public ConfigAdapter getAdapter() {
        return switch (appProperties.configSource()) {
            case "azure" -> azureConfigAdapter;
            case "yaml" -> yamlConfigAdapter;
            default ->
                    throw new IllegalArgumentException("Invalid Config Adapter Provider: " + appProperties.configSource());
        };
    }

}
