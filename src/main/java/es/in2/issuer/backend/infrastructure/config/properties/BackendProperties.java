package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "backend")
@Validated
public record BackendProperties(
        @NotNull String url,
        @NotNull String configSource) {

    @ConstructorBinding
    public BackendProperties(String url, String configSource) {
        this.url = url;
        this.configSource = configSource;
    }
}
