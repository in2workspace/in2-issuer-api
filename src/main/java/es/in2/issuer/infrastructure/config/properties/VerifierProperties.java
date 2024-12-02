package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "verifier")
@Validated
public record VerifierProperties(
        @NotBlank String externalDomain
) {
    @ConstructorBinding
    public VerifierProperties(String externalDomain) {
        this.externalDomain = externalDomain;
    }

}
