package es.in2.issuer.configuration.model;

public enum ConfigProviderName {
    AZURE("azure"),
    YAML("yaml");

    private final String providerName;

    ConfigProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public String toString() {
        return providerName;
    }
}
