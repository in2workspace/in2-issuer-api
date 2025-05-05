package es.in2.issuer.backend.shared.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;
import java.util.Optional;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
        @NotBlank @URL List<String> defaultAllowedOrigins,
        @NotBlank @URL List<String> externalAllowedOrigins) {

    @ConstructorBinding
    public CorsProperties(List<String> defaultAllowedOrigins, List<String> externalAllowedOrigins) {
        this.defaultAllowedOrigins = Optional.ofNullable(defaultAllowedOrigins).orElse(List.of());
        this.externalAllowedOrigins = Optional.ofNullable(externalAllowedOrigins).orElse(List.of());
    }
}
