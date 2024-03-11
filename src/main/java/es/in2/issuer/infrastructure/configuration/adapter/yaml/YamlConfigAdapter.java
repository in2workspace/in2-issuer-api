package es.in2.issuer.infrastructure.configuration.adapter.yaml;

import es.in2.issuer.infrastructure.configuration.model.ConfigProviderName;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import es.in2.issuer.infrastructure.configuration.util.ConfigSourceName;
import org.springframework.stereotype.Component;

@Component
@ConfigSourceName(name = ConfigProviderName.YAML)
public class YamlConfigAdapter implements GenericConfigAdapter {

    @Override
    public String getConfiguration(String key){
        return key;
    }

}
