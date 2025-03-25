package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "remote-signature")
@Validated
public record RemoteSignatureProperties(
        @NotNull String domain,
        @NotNull Paths paths
) {

    @ConstructorBinding
    public RemoteSignatureProperties(String domain, Paths paths) {
        this.domain = domain;
        this.paths = Optional.ofNullable(paths).orElse(new Paths(""));
    }

    @Validated
    public record Paths(@NotNull String signPath) {
    }

}