package es.in2.issuer.infrastructure.config.adapter.factory;

import es.in2.issuer.infrastructure.config.adapter.exception.ConfigAdapterFactoryException;
import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigAdapterFactory {
    List<ConfigAdapter> configAdapters;

    public ConfigAdapterFactory(List<ConfigAdapter> configServices) {
        this.configAdapters = configServices;
    }

    public ConfigAdapter getAdapter() {
        //check if a list is empty or the size is greater than 1
        if (configAdapters.size() != 1) {
            throw new ConfigAdapterFactoryException(configAdapters.size());
        }
        return configAdapters.get(0);
    }

}
