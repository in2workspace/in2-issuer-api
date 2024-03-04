package es.in2.issuer.configuration.adapter.azure.config.properties;

import es.in2.issuer.configuration.model.ConfigProviderName;
import es.in2.issuer.configuration.util.ConfigSourceName;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;
@ConfigSourceName(name = ConfigProviderName.AZURE)
@ConfigurationProperties(prefix = "azure.app.config")
@Validated
public record AzureProperties(
        @NotNull(message = "Endpoint is mandatory") String endpoint,
        @NestedConfigurationProperty AzurePropertiesLabel label
) {
    @ConstructorBinding
    public AzureProperties(String endpoint, AzurePropertiesLabel label) {
        this.endpoint = endpoint;
        this.label = Optional.ofNullable(label).orElse(new AzurePropertiesLabel(null));
    }
}