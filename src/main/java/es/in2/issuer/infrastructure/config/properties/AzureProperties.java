package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "azure")
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

    public record AzurePropertiesLabel(String global) {
    }

}