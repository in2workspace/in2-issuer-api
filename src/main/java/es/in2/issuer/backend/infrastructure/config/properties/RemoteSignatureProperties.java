package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "remote-signature")
@Validated
public record RemoteSignatureProperties(
        @NotBlank String type,
        @NotBlank @URL String url,
        @NotNull Paths paths,
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String credentialId,
        @NotBlank String credentialPassword
) {

    @ConstructorBinding
    public RemoteSignatureProperties(String type, String url, Paths paths, String clientId, String clientSecret, String credentialId, String credentialPassword) {
        this.url = url;
        this.paths = Optional.ofNullable(paths).orElse(new Paths(""));
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.credentialId = credentialId;
        this.credentialPassword = credentialPassword;
        this.type = type;
    }

    @Validated
    public record Paths(@NotBlank String signPath) {
    }

}