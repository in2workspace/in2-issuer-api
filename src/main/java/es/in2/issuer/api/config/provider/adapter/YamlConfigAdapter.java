package es.in2.issuer.api.config.provider.adapter;

import es.in2.issuer.api.config.provider.ConfigSourceName;
import es.in2.issuer.api.config.provider.GenericConfigAdapter;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = "yaml")

public class YamlConfigAdapter implements GenericConfigAdapter {
    @Override
    public String getBaseUrl() {
        return "http://localhost:8080";
    }

}
