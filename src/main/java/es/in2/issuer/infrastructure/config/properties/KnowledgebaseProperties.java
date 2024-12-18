package es.in2.issuer.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "knowledgebase")
@Validated
public record KnowledgebaseProperties(
        @NotNull String url
) {
}
