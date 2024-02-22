package es.in2.issuer.configuration.azure.config.properties;

import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;

@ConfigSourceName(name = ConfigProviderName.AZURE)
public record AzurePropertiesLabel(String global) {
}
