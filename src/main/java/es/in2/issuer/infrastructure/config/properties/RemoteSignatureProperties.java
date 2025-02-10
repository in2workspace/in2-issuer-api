package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "remote-signature")
@Validated
public record RemoteSignatureProperties(
        @NotNull String domain,
        @NotNull Paths paths,
        String clientId,
        String clientSecret,
        String credentialId,
        String credentialPassword,
        @NotNull String externalService
) {

    @ConstructorBinding
    public RemoteSignatureProperties(String domain, Paths paths, String clientId, String clientSecret, String credentialId, String credentialPassword, String externalService) {
        this.domain = domain;
        this.paths = Optional.ofNullable(paths).orElse(new Paths(""));
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.credentialId = credentialId;
        this.credentialPassword = credentialPassword;
        this.externalService = externalService;
    }

    @Validated
    public record Paths(@NotNull String signPath) {
    }

}