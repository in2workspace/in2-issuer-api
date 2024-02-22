package es.in2.issuer.api.config.provider;

public enum ConfigProviderNameEnum {
    AZURE("azure"),
    YAML("yaml");

    private final String providerName;

    ConfigProviderNameEnum(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public String toString() {
        return providerName;
    }
}
