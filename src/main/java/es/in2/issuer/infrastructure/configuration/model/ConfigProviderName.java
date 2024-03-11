package es.in2.issuer.infrastructure.configuration.model;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
public enum ConfigProviderName {
    AZURE("azure"),
    YAML("yaml");

    private final String providerName;

    @Override
    public String toString() {
        return providerName;
    }

}
