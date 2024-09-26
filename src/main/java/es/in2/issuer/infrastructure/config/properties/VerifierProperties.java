package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "verifier")
@Validated
public record VerifierProperties(
        @NotNull String externalDomain,
        @NotNull String internalDomain,
        @NotNull String key,
        @NotNull VerifierProperties.Paths paths
) {
    @ConstructorBinding
    public VerifierProperties(String externalDomain, String internalDomain, String key, VerifierProperties.Paths paths) {
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.key = key;
        this.paths = Optional.ofNullable(paths).orElse(new VerifierProperties.Paths(""));

    }

    @Validated
    public record Paths(@NotNull String tokenPath) {
    }
}
