package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.configs")
@Validated
public record AppConfigurationProperties(
        @NotNull String iamExternalDomain,
        @NotNull String iamInternalDomain,
        @NotNull String issuerExternalDomain,
        @NotNull String authenticSourcesDomain,
        @NotNull String keyVaultDomain,
        @NotNull String remoteSignatureDomain,
        @NotNull String issuerDid,
        @NotNull String jwtDecoderPath,
        @NotNull String jwtValidator,
        @NotNull String jwtDecoderLocalPath,
        @NotNull String preAuthCodeUriTemplate,
        @NotNull String tokenUriTemplate,
        @NotNull String nonceValidationEndpoint
) {}

