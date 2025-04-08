package es.in2.issuer.shared.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "trust-framework")
@Validated
public record TrustFrameworkProperties(@NotNull String url) {
}
