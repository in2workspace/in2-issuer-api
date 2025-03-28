package es.in2.issuer.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "hashicorp.vault")
@Validated
public record HashicorpVaultProperties(String path,
                                       String host,
                                       String port,
                                       String scheme,
                                       String token) {

}
