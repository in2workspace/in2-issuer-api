package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@ConfigurationProperties(prefix = "api")
@Validated
public record ApiProperties(
        @NotNull String externalDomain,
        @NotNull String internalDomain,
        @NotNull String configSource,
        @NotNull MemoryCache cacheLifetime
) {

    @ConstructorBinding
    public ApiProperties(String externalDomain, String internalDomain, String configSource, MemoryCache cacheLifetime) {
        this.externalDomain = externalDomain;
        this.internalDomain = internalDomain;
        this.configSource = configSource;
        this.cacheLifetime = Optional.ofNullable(cacheLifetime)
                .orElse(new MemoryCache(10, 10));
    }

    public record MemoryCache(
            @NotNull long credentialOffer,
            @NotNull long verifiableCredential) {
    }

}
