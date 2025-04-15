package es.in2.issuer.backend.infrastructure.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "knowledge-base")
@Validated
public record KnowledgeBaseProperties(
        @NotBlank @URL String walletGuideUrl,
        @NotBlank @URL String uploadCertificationGuideUrl
) {
}
