package es.in2.issuer.api.config.provider;

public class ConfigProviderImpl implements ConfigProvider {
    private final GenericConfigAdapter genericConfigAdapter;

    public ConfigProviderImpl(ConfigAdapterFactory configAdapterFactory){
        this.genericConfigAdapter = configAdapterFactory.getAdapter();
    }
    @Override
    public String getBaseUrl() {
        return genericConfigAdapter.getBaseUrl();
    }
}
