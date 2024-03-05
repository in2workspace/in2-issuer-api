package es.in2.issuer.configuration.util;
import es.in2.issuer.configuration.exception.ConfigAdapterFactoryException;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConfigAdapterFactory {
    List<GenericConfigAdapter> configAdapters;

    public ConfigAdapterFactory(List<GenericConfigAdapter> configServices) {
        this.configAdapters = configServices;
    }

    public GenericConfigAdapter getAdapter() {
        //check if list is empty or size is greater than 1
        if (configAdapters.isEmpty() || configAdapters.size() > 1) {
            throw new ConfigAdapterFactoryException(configAdapters.size());
        }

        return configAdapters.get(0);
    }
}
