package es.in2.issuer.infrastructure.config.security;

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

import static es.in2.issuer.domain.util.EndpointsConstants.*;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationManager customAuthenticationManager;
    private final ReactiveJwtDecoder internalJwtDecoder;
    private final DefaultCORSConfig defaultCORSConfig;
    private final ExternalServicesCORSConfig externalServicesCORSConfig;


    @Bean
    public AuthenticationWebFilter customAuthenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(customAuthenticationManager);
        // Set the path for which the filter will be applied
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers("/vci/v1/issuances/**")
        );
        // Configure the Bearer token authentication converter
        ServerBearerTokenAuthenticationConverter bearerConverter = new ServerBearerTokenAuthenticationConverter();
        authenticationWebFilter.setServerAuthenticationConverter(bearerConverter);

        return authenticationWebFilter;
    }

    // Security configuration for specific endpoints
    @Bean
    @Order(1)
    public SecurityWebFilterChain externalServicesFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> externalServicesCORSConfig.externalCorsConfigurationSource())
                .authorizeExchange(exchanges -> exchanges
                                .pathMatchers(HttpMethod.POST, "/vci/v1/issuances/**").authenticated()
                                .pathMatchers(HttpMethod.GET, getSwaggerPaths()).permitAll()
                                .pathMatchers(HttpMethod.GET, PUBLIC_HEALTH).permitAll()
                                .pathMatchers(HttpMethod.GET, PUBLIC_CREDENTIAL_OFFER).permitAll()
                                .pathMatchers(HttpMethod.GET, PUBLIC_DISCOVERY_ISSUER).permitAll()
                                .pathMatchers(HttpMethod.GET, PUBLIC_DISCOVERY_AUTH_SERVER).permitAll()
                                .pathMatchers(HttpMethod.POST, "/token").permitAll()
                                .pathMatchers(HttpMethod.POST, "/api/v1/deferred-credentials").permitAll()
                                .pathMatchers(HttpMethod.GET,  "/api/v1/deferred-credentials").permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(customAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    // General security configuration for other endpoints
    @Bean
    @Order(2)
    public SecurityWebFilterChain defaultFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> defaultCORSConfig.defaultCorsConfigurationSource())
                .authorizeExchange(exchanges -> exchanges
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
