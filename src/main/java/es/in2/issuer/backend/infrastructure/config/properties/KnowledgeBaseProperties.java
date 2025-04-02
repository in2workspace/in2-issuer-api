package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "knowledge-base")
@Validated
public record KnowledgeBaseProperties(
        @NotNull String walletUrl,
        @NotNull String uploadCertificationGuideUrl
) {
}
