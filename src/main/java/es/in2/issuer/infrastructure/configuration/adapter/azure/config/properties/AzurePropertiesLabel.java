package es.in2.issuer.infrastructure.configuration.adapter.azure.config.properties;

import es.in2.issuer.infrastructure.configuration.model.ConfigProviderName;
import es.in2.issuer.infrastructure.configuration.util.ConfigSourceName;

@ConfigSourceName(name = ConfigProviderName.AZURE)
public record AzurePropertiesLabel(String global) {
}
