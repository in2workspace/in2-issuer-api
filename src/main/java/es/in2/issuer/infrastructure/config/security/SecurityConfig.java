package es.in2.issuer.infrastructure.config.security;

import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

import static es.in2.issuer.domain.util.EndpointsConstants.*;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppConfig appConfig;
    private final CustomAuthenticationManager customAuthenticationManager;
    private final ReactiveJwtDecoder internalJwtDecoder;


    @Bean
    public AuthenticationWebFilter customAuthenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(customAuthenticationManager);
        authenticationWebFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());

        // Configure the Bearer token authentication converter
        ServerBearerTokenAuthenticationConverter bearerConverter = new ServerBearerTokenAuthenticationConverter();
        authenticationWebFilter.setServerAuthenticationConverter(bearerConverter);

        return authenticationWebFilter;
    }

    // Security configuration for specific endpoints
    @Bean
    @Order(1)
    public SecurityWebFilterChain issuancesFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/vci/v1/issuances/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/vci/v1/issuances").authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(customAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    // General security configuration for other endpoints
    @Bean
    @Order(2)
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(getSwaggerPaths()).permitAll()
                        .pathMatchers(PUBLIC_HEALTH).permitAll()
                        .pathMatchers(PUBLIC_CREDENTIAL_OFFER).permitAll()
                        .pathMatchers(PUBLIC_DISCOVERY_ISSUER).permitAll()
                        .pathMatchers(PUBLIC_DISCOVERY_AUTH_SERVER).permitAll()
                        .pathMatchers(HttpMethod.POST, "/token").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/deferred-credentials").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/deferred-credentials").permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer
                                .jwt(jwtSpec -> jwtSpec
                                        .jwtDecoder(internalJwtDecoder))

                );
        return http.build();
    }

    // CORS configuration to allow requests from specified origins
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // Allow specified origins
        corsConfig.setAllowedOrigins(List.of(
                appConfig.getIssuerUiExternalDomain(),
                "http://localhost:4200",
                "http://localhost:8080"
        ));
        // Allow specified HTTP methods
        corsConfig.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.HEAD.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        // Set max age for preflight requests
        corsConfig.setMaxAge(1800L);
        // Allow all headers
        corsConfig.addAllowedHeader("*");
        corsConfig.addExposedHeader("*");
        // Allow credentials (cookies, authorization headers, etc.)
        corsConfig.setAllowCredentials(true);
        // Apply the configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    // Helper method to get Swagger-related paths for permitting access
    private String[] getSwaggerPaths() {
        return new String[]{
                SWAGGER_UI,
                SWAGGER_RESOURCES,
                SWAGGER_API_DOCS,
                SWAGGER_SPRING_UI,
                SWAGGER_WEBJARS
        };
    }
}
