package es.in2.issuer.api.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Profile("azure")
@ConfigurationProperties(prefix = "azure.app.config")
@Validated
public record AzureProperties(
        @NotNull(message = "Endpoint is mandatory") String endpoint,
        @NestedConfigurationProperty String label
) {
    @ConstructorBinding
    public AzureProperties(String endpoint, String label) {
        this.endpoint = endpoint;
        this.label = label;
    }
}