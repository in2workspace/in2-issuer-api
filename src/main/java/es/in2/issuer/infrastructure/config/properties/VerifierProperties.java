package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "verifier")
@Validated
public record VerifierProperties(
        @NotBlank String didKey,
        @NotBlank String externalDomain,
        @NotNull VerifierProperties.Paths paths
) {
    @ConstructorBinding
    public VerifierProperties(String didKey, String externalDomain, VerifierProperties.Paths paths) {
        this.didKey = didKey;
        this.externalDomain = externalDomain;
        this.paths = Optional.ofNullable(paths).orElse(new VerifierProperties.Paths(""));
    }


    @Validated
    public record Token(@NotBlank String expirationUnitTime, @NotBlank String expiration) {
    }


    @Validated
    public record Paths(@NotBlank String wellKnownPath) {
    }
}
