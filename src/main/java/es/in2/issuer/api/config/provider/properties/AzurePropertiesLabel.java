package es.in2.issuer.api.config.provider.properties;

import es.in2.issuer.api.config.provider.ConfigProviderNameEnum;
import es.in2.issuer.api.config.provider.ConfigSourceName;
import org.springframework.context.annotation.Profile;

@ConfigSourceName(name = ConfigProviderNameEnum.AZURE)
public record AzurePropertiesLabel(String global) {
}
