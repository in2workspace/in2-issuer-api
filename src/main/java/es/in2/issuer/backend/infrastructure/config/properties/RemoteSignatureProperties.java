package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "remote-signature")
@Validated
public record RemoteSignatureProperties(
        @NotNull String type,
        @NotNull String url,
        @NotNull Paths paths,
        String clientId,
        String clientSecret,
        String credentialId,
        String credentialPassword
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
    public record Paths(@NotNull String signPath) {
    }

}