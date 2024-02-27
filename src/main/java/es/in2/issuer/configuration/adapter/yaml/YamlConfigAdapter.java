package es.in2.issuer.configuration.adapter.yaml;

import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderName.YAML)
public class YamlConfigAdapter implements GenericConfigAdapter {
    @Override
    public String getConfiguration(String key){
        return key;
    }
}
