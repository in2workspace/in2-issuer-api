package es.in2.issuer.shared.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "verifier")
@Validated
public record VerifierProperties(
        @NotBlank String externalDomain
) {
}
