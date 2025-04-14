package es.in2.issuer.backend.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

import static es.in2.issuer.backend.domain.util.EndpointsConstants.*;

@Configuration
@RequiredArgsConstructor
public class PublicCORSConfig {

    /**
     * Public CORS configuration source.
     */
    @Bean
    public UrlBasedCorsConfigurationSource publicCorsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(PUBLIC_CREDENTIAL_OFFER, configuration);
        source.registerCorsConfiguration(PUBLIC_DISCOVERY_ISSUER, configuration);
        source.registerCorsConfiguration(DEFERRED_CREDENTIALS, configuration);
        source.registerCorsConfiguration(TOKEN, configuration);
        source.registerCorsConfiguration(PROMETHEUS, configuration);
        return source;
    }
}
