package es.in2.issuer.backend.infrastructure.config.properties;

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
        @NotNull Paths paths,
        @NotNull Client client

) {

    @ConstructorBinding
    public AuthServerProperties(String provider, String externalDomain, String internalDomain, String realm, Paths paths, Client client) {
        this.provider = provider;
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.realm = realm;
        this.paths = Optional.ofNullable(paths).orElse(
                new Paths("", "", "", "", "", "", ""));
        this.client = Optional.ofNullable(client).orElse(
                new Client("","","")
        );
    }

    @Validated
    public record Paths(
            @NotNull String issuerDid,
            @NotNull String jwtDecoderPath,
            @NotNull String jwtDecoderLocalPath,
            @NotNull String jwtValidatorPath,
            @NotNull String preAuthorizedCodePath,
            @NotNull String tokenPath,
            @NotNull String nonceValidationPath
    ) {
    }
    @Validated
    public record Client(
            @NotNull String clientId,
            @NotNull String username,
            @NotNull String password
    ){

    }

}
