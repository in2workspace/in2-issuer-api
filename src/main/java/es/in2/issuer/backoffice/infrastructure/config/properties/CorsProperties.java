package es.in2.issuer.backoffice.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;
import java.util.Optional;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(List<String> defaultAllowedOrigins, List<String> externalAllowedOrigins) {

    @ConstructorBinding
    public CorsProperties(List<String> defaultAllowedOrigins, List<String> externalAllowedOrigins) {
        this.defaultAllowedOrigins = Optional.ofNullable(defaultAllowedOrigins).orElse(List.of());
        this.externalAllowedOrigins = Optional.ofNullable(externalAllowedOrigins).orElse(List.of());
    }
}
