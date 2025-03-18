package es.in2.issuer.infrastructure.config.security;

import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

import static es.in2.issuer.domain.util.EndpointsConstants.*;

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
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(PUBLIC_CREDENTIAL_OFFER, configuration);
        source.registerCorsConfiguration(PUBLIC_DISCOVERY_ISSUER, configuration);
        source.registerCorsConfiguration(EXTERNAL_ISSUANCE, configuration);
        source.registerCorsConfiguration(DEFERRED_CREDENTIALS, configuration);
        source.registerCorsConfiguration(TOKEN, configuration);
        return source;
    }
}
