package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "authentic-sources.routes")
@Validated
public record AuthenticSourcesProperties(
        @NotNull String getUser
) {
}
