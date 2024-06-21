package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "remote-signature")
@Validated
public record RemoteSignatureProperties(
        @NotNull String externalDomain,
        @NotNull String internalDomain,
        @NotNull Paths paths
) {

    @ConstructorBinding
    public RemoteSignatureProperties(String externalDomain, String internalDomain, Paths paths) {
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.paths = Optional.ofNullable(paths).orElse(new Paths(""));
    }

    @Validated
    public record Paths(@NotNull String signPath) {
    }

}