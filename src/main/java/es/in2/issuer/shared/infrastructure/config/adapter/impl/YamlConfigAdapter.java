package es.in2.issuer.shared.infrastructure.config.adapter.impl;

import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import org.springframework.stereotype.Component;

@Component
public class YamlConfigAdapter implements ConfigAdapter {

    @Override
    public String getConfiguration(String key){
        return key;
    }

}
