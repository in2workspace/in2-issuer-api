package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "issuer-identity")
@Validated
public record IssuerIdentityProperties(
        @NotBlank String credentialSubjectDidKey,
        @NotBlank String jwtCredential,
        @NotNull Crypto crypto,
        @NotNull ClientAssertion clientAssertion
) {
    @ConstructorBinding
    public IssuerIdentityProperties(
            String credentialSubjectDidKey,
            String jwtCredential,
            Crypto crypto,
            ClientAssertion clientAssertion) {
        this.credentialSubjectDidKey = credentialSubjectDidKey;
        this.jwtCredential = jwtCredential;
        this.crypto = crypto;
        this.clientAssertion = Optional.ofNullable(clientAssertion).orElse(new ClientAssertion("2", "Minutes"));
    }

    @Validated
    public record ClientAssertion(@NotBlank String expiration, @NotBlank String expirationUnitTime) {
    }

    @Validated
    public record Crypto(@NotBlank String privateKey) {
    }
}

