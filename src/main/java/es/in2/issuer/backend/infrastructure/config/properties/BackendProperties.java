package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "backend")
@Validated
public record BackendProperties(
        @NotBlank @URL String url,
        @NotBlank String configSource) {

    @ConstructorBinding
    public BackendProperties(String url, String configSource) {
        this.url = url;
        this.configSource = configSource;
    }
}
