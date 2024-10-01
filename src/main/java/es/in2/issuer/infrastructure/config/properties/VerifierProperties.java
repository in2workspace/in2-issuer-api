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
        @NotNull String verifierKey,
        @NotNull String vc,
        @NotNull VerifierProperties.Crypto crypto,
        @NotNull VerifierProperties.ClientAssertion clientAssertion,
        @NotNull VerifierProperties.Paths paths
) {
    @ConstructorBinding
    public VerifierProperties(String externalDomain, String internalDomain, String verifierKey, String vc, VerifierProperties.Crypto crypto, VerifierProperties.ClientAssertion clientAssertion, VerifierProperties.Paths paths) {
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.verifierKey = verifierKey;
        this.vc = vc;
        this.crypto = crypto;
        this.clientAssertion = clientAssertion;
        this.paths = Optional.ofNullable(paths).orElse(new VerifierProperties.Paths(""));

    }

    @Validated
    public record ClientAssertion(@NotNull Token token) {
    }

    @Validated
    public record Token(@NotNull String cronUnit, Integer expiration) {
    }

    @Validated
    public record Crypto(@NotNull String privateKey) {
    }

    @Validated
    public record Paths(@NotNull String tokenPath) {
    }
}
