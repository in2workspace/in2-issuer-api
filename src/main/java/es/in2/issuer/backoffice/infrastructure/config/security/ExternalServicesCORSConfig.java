package es.in2.issuer.backoffice.infrastructure.config.security;

import es.in2.issuer.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

import static es.in2.issuer.shared.domain.util.EndpointsConstants.*;

@Configuration
@RequiredArgsConstructor
public class ExternalServicesCORSConfig {

    private final AppConfig appConfig;

    /**
     * External CORS configuration source.
     */
    @Bean
    public UrlBasedCorsConfigurationSource externalCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appConfig.getExternalCorsAllowedOrigins());
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(EXTERNAL_ISSUANCE, configuration);
        return source;
    }
}
