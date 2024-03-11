package es.in2.issuer.infrastructure.configuration.util;

import es.in2.issuer.infrastructure.configuration.exception.ConfigAdapterFactoryException;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigAdapterFactory {
    List<GenericConfigAdapter> configAdapters;

    public ConfigAdapterFactory(List<GenericConfigAdapter> configServices) {
        this.configAdapters = configServices;
    }

    public GenericConfigAdapter getAdapter() {
        //check if a list is empty or the size is greater than 1
        if (configAdapters.size() != 1) {
            throw new ConfigAdapterFactoryException(configAdapters.size());
        }
        return configAdapters.get(0);
    }

}
