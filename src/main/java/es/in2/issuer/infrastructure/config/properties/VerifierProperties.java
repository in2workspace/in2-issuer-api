package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "verifier")
@Validated
public record VerifierProperties(
        @NotNull String verifierDidKey,
        @NotNull String externalDomain,
        @NotNull String credentialSubjectKey,
        @NotNull String vc,
        @NotNull VerifierProperties.Crypto crypto,
        @NotNull VerifierProperties.ClientAssertion clientAssertion
) {
    @ConstructorBinding
    public VerifierProperties(String verifierDidKey, String externalDomain, String credentialSubjectKey, String vc, VerifierProperties.Crypto crypto, VerifierProperties.ClientAssertion clientAssertion) {
        this.verifierDidKey = verifierDidKey;
        this.externalDomain = externalDomain;
        this.credentialSubjectKey = credentialSubjectKey;
        this.vc = vc;
        this.crypto = Optional.ofNullable(crypto).orElse(new VerifierProperties.Crypto(""));
        this.clientAssertion = Optional.ofNullable(clientAssertion).orElse(new VerifierProperties.ClientAssertion(new Token("",0)));
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
}
