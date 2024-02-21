package es.in2.issuer.api.config.provider.adapter;

import es.in2.issuer.api.config.provider.ConfigSourceName;
import es.in2.issuer.api.config.provider.GenericConfigAdapter;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = "azure")

public class AzureConfigAdapter implements GenericConfigAdapter {
    @Override
    public String getBaseUrl() {
        return "baseUrl from Azure";
    }
}
