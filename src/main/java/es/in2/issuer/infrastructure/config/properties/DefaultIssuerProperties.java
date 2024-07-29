package es.in2.issuer.infrastructure.config.properties;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "default-issuer")
@Validated
public record  DefaultIssuerProperties (
    @NotNull String commonName,
    @NotNull String country,
    @NotNull String email,
    @NotNull String organizationIdentifier,
    @NotNull String organization,
    @NotNull String serialNumber

) {}
