package es.in2.issuer.infrastructure.config.security;

import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

import static es.in2.issuer.domain.util.EndpointsConstants.PUBLIC_CREDENTIAL_OFFER;
import static es.in2.issuer.domain.util.EndpointsConstants.PUBLIC_DISCOVERY_ISSUER;

@Configuration
@RequiredArgsConstructor
public class ExternalServicesCORSConfig {

    private final AppConfig appConfig;

    /**
     * External CORS configuration source.
     */
    @Bean
    public UrlBasedCorsConfigurationSource externalCorsConfigurationSource() {
//        for (String origin : appConfig.getDefaultCorsAllowedOrigins()) {
//            System.out.println("external cors: " + origin);
//        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(PUBLIC_CREDENTIAL_OFFER, configuration);
        source.registerCorsConfiguration(PUBLIC_DISCOVERY_ISSUER, configuration);
        source.registerCorsConfiguration("/api/v1/issuances", configuration);
        source.registerCorsConfiguration("/api/v1/deferred-credentials", configuration);
        source.registerCorsConfiguration("/token", configuration);
        return source;
    }
}
