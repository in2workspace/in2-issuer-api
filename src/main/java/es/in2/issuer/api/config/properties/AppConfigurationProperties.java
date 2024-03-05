package es.in2.issuer.api.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.configs")
@Validated
public record AppConfigurationProperties(
        @NotNull String iamExternalDomain,
        @NotNull String issuerDomain,
        @NotNull String authenticSourcesDomain,
        @NotNull String keyVaultDomain,
        @NotNull String remoteSignatureDomain,
        @NotNull String iamDid,
        @NotNull String issuerDid
) {}

