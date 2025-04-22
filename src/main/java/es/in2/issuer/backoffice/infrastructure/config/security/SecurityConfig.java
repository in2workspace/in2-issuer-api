package es.in2.issuer.backoffice.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import static es.in2.issuer.shared.domain.util.EndpointsConstants.*;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationManager customAuthenticationManager;
    private final ReactiveJwtDecoder internalJwtDecoder;
    private final InternalCORSConfig internalCORSConfig;
    private final ExternalServicesCORSConfig externalServicesCORSConfig;
    private final PublicCORSConfig publicCORSConfig;
    private final Oid4vciCORSConfig oid4VciCORSConfig;
    private final Oidc4vciAuthenticationManager oidc4vciAuthenticationManager;


    @Bean
    @Primary
    public ReactiveAuthenticationManager primaryAuthenticationManager() {
        return customAuthenticationManager;
    }

    @Bean
    public AuthenticationWebFilter customAuthenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(customAuthenticationManager);
        // Set the path for which the filter will be applied
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers(EXTERNAL_ISSUANCE)
        );
        // Configure the Bearer token authentication converter
        ServerBearerTokenAuthenticationConverter bearerConverter = new ServerBearerTokenAuthenticationConverter();
        authenticationWebFilter.setServerAuthenticationConverter(bearerConverter);

        return authenticationWebFilter;
    }

    // Public filter chain for public endpoints
    @Bean
    @Order(1)
    public SecurityWebFilterChain publicFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        SWAGGER_UI,
                        SWAGGER_RESOURCES,
                        SWAGGER_API_DOCS,
                        SWAGGER_SPRING_UI,
                        SWAGGER_WEBJARS,
                        PUBLIC_HEALTH,
                        PUBLIC_CREDENTIAL_OFFER,
                        PUBLIC_DISCOVERY_ISSUER,
                        PUBLIC_DISCOVERY_AUTH_SERVER,
                        TOKEN,
                        DEFERRED_CREDENTIALS,
                        PROMETHEUS
                ))
                .cors(cors -> cors.configurationSource(publicCORSConfig.publicCorsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    // External filter chain for external endpoints
    @Bean
    @Order(2)
    public SecurityWebFilterChain externalFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, EXTERNAL_ISSUANCE))
                .cors(cors -> cors.configurationSource(externalServicesCORSConfig.externalCorsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(customAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /*@Bean
    @Order(3)
    public SecurityWebFilterChain oidc4vciFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(OID4VCI))
                .cors(cors -> oid4VciCORSConfig.oid4vciCorsConfigurationSource())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(TOKEN, CREDENTIAL_OFFER).permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .addFilterAt(oid4vciBearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }*/

    // Internal security configuration for internal endpoints
    @Bean
    @Order(4)
    public SecurityWebFilterChain internalFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.anyExchange())
                .cors(cors -> internalCORSConfig.defaultCorsConfigurationSource())
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

    private AuthenticationWebFilter oid4vciBearerAuthenticationFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(oidc4vciAuthenticationManager);

        // Set the path for which the filter will be applied
        ServerWebExchangeMatcher excludedPaths = ServerWebExchangeMatchers.pathMatchers(TOKEN, CREDENTIAL_OFFER);
        authenticationWebFilter.setRequiresAuthenticationMatcher(
                new NegatedServerWebExchangeMatcher(excludedPaths)
        );
        // Configure the Bearer token authentication converter
        ServerBearerTokenAuthenticationConverter bearerConverter = new ServerBearerTokenAuthenticationConverter();
        authenticationWebFilter.setServerAuthenticationConverter(bearerConverter);

        return authenticationWebFilter;
    }
}