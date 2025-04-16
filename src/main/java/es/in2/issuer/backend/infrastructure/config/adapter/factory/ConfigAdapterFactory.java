package es.in2.issuer.backend.infrastructure.config.adapter.factory;

import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.impl.YamlConfigAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConfigAdapterFactory {

    private final AppConfig appConfig;
    private final AzureConfigAdapter azureConfigAdapter;
    private final YamlConfigAdapter yamlConfigAdapter;

    public ConfigAdapter getAdapter() {
        return switch (appConfig.getConfigSource()) {
            case "azure" -> azureConfigAdapter;
            case "yaml" -> yamlConfigAdapter;
            default ->
                    throw new IllegalArgumentException("Invalid Config Adapter Provider: " + appConfig.getConfigSource());
        };
    }

}
