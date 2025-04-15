package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "issuer-frontend")
@Validated
public record IssuerFrontendProperties(
        @NotBlank @URL String url
) {
}
