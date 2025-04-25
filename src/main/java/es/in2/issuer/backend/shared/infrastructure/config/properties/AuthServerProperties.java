package es.in2.issuer.backend.shared.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "auth-server")
@Validated
public record AuthServerProperties(
        @NotBlank String provider,
        @NotBlank @URL String externalUrl,
        @NotBlank @URL String internalUrl,
        @NotBlank String realm,
        @NotBlank Paths paths,
        @NotNull Client client

) {

    @ConstructorBinding
    public AuthServerProperties(String provider, String externalUrl, String internalUrl, String realm, Paths paths, Client client) {
        this.provider = provider;
        this.externalUrl = externalUrl;
        this.internalUrl = internalUrl;
        this.realm = realm;
        this.paths = Optional.ofNullable(paths).orElse(
                new Paths("", "", "", "", "", "", ""));
        this.client = Optional.ofNullable(client).orElse(
                new Client("","","")
        );
    }

    @Validated
    public record Paths(
            @NotBlank String issuerDid,
            @NotBlank String jwtDecoderPath,
            @NotBlank String jwtDecoderLocalPath,
            @NotBlank String jwtValidatorPath,
            @NotBlank String preAuthorizedCodePath,
            @NotBlank String tokenPath,
            @NotBlank String nonceValidationPath
    ) {
    }
    @Validated
    public record Client(
            @NotBlank String clientId,
            @NotBlank String username,
            @NotBlank String password
    ){

    }

}
