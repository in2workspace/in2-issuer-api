package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "auth-server")
@Validated
public record AuthServerProperties(
        @NotNull String provider,
        @NotNull String externalDomain,
        @NotNull String internalDomain,
        @NotNull String realm,
        @NotNull Paths paths
) {

    @ConstructorBinding
    public AuthServerProperties(String provider, String externalDomain, String internalDomain, String realm, Paths paths) {
        this.provider = provider;
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.realm = realm;
        this.paths = Optional.ofNullable(paths).orElse(
                new Paths("", "", "", "", "", "", ""));
    }

    @Validated
    public record Paths(
            @NotNull String issuerDid,
            @NotNull String jwtDecoderPath,
            @NotNull String jwtDecoderLocalPath,
            @NotNull String jwtValidator,
            @NotNull String preAuthorizedCodePath,
            @NotNull String tokenPath,
            @NotNull String nonceValidationPath
    ) {
    }

}
