package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "trust-service-provider-for-certifications")
@Validated
public record TrustServiceProviderForCertificationsProperties(
        @NotNull String did
) {
}
