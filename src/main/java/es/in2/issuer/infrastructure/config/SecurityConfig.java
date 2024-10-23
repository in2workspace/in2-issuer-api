package es.in2.issuer.infrastructure.config;

import es.in2.issuer.domain.service.M2MTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

import static es.in2.issuer.domain.util.EndpointsConstants.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthServerConfig authServerConfig;
    private final AppConfig appConfig;
    private final M2MTokenService m2MTokenService;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(authServerConfig.getJwtDecoder())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(authServerConfig.getJwtValidator()));
        return jwtDecoder;
    }

    @Bean
    @Order(1)
    public SecurityWebFilterChain issuancesFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/vci/v1/issuances/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/vci/v1/issuances").authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwtSpec -> jwtSpec
                                .jwtDecoder(jwtDecoder())
                                .authenticationManager(customAuthenticationManager())
                        )
                );
        return http.build();
    }

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
                        .pathMatchers(HttpMethod.POST,  "/token").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/deferred-credentials").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/deferred-credentials").permitAll()
                        .anyExchange().authenticated()
                ).csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer
                                .jwt(withDefaults()));
        return http.build();
    }

//    @Bean
//    public ReactiveAuthenticationManager customAuthenticationManager() {
//        return authentication -> jwtDecoder().decode(authentication.getCredentials().toString())
//                .flatMap(jwt -> Mono.just(new JwtAuthenticationToken(jwt, null, null)))
//                .cast(Authentication.class)
//                .onErrorResume(ex -> {
//                    String token = authentication.getCredentials().toString();
//                    return m2MTokenService.verifyM2MToken(token)
//                            .then(Mono.defer(() -> {
//                                UsernamePasswordAuthenticationToken customToken =
//                                        new UsernamePasswordAuthenticationToken(null, null, null);
//                                return Mono.just(customToken).cast(Authentication.class);
//                            }))
//                            .onErrorResume(ex2 -> Mono.error(new BadCredentialsException("Invalid M2M Token")));
//                });
//    }

    @Bean
    public ReactiveAuthenticationManager customAuthenticationManager() {
        return authentication -> {
            try {
                // Try to validate token with default decoder
                return jwtDecoder().decode(authentication.getCredentials().toString())
                        .flatMap(jwt -> Mono.just(new JwtAuthenticationToken(jwt, null, null)))
                        .cast(Authentication.class)
                        .onErrorResume(ex -> {
                            // If default decoder fail, try to validate as M2M token
                            String token = authentication.getCredentials().toString();
                            return m2MTokenService.verifyM2MToken(token)
                                    .then(Mono.defer(() -> {
                                        UsernamePasswordAuthenticationToken customToken =
                                                new UsernamePasswordAuthenticationToken(null, null, null);
                                        return Mono.just(customToken).cast(Authentication.class);
                                    }))
                                    .onErrorResume(ex2 -> Mono.error(new BadCredentialsException("Invalid M2M Token")));
                        });
            } catch (Exception e) {
                return Mono.error(new BadCredentialsException("Authentication failed", e));
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(List.of(appConfig.getIssuerUiExternalDomain(), "http://localhost:4200", "http://localhost:8080"));
        corsConfig.setMaxAge(8000L);
        corsConfig.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.HEAD.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()));
        corsConfig.setMaxAge(1800L);
        corsConfig.addAllowedHeader("*");
        corsConfig.addExposedHeader("*");
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig); // Apply the configuration to all paths
        return source;
    }

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