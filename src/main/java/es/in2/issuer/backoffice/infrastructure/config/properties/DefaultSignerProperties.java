package es.in2.issuer.backoffice.infrastructure.config.properties;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "default-signer")
@Validated
public record DefaultSignerProperties(
        @NotNull String commonName,
        @NotNull String country,
        @NotNull String email,
        @NotNull String organizationIdentifier,
        @NotNull String organization,
        @NotNull String serialNumber

) {}
