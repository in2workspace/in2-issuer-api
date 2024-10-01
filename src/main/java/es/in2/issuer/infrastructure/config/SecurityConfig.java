package es.in2.issuer.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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

    // fixme: why we need to use different jwtDecoder for sbx and dev-prod?
    @Bean
    @Profile("!sbx")
    public ReactiveJwtDecoder jwtDecoder() {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri(authServerConfig.getJwtDecoder())
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(authServerConfig.getJwtValidator()));
        return jwtDecoder;
    }

    @Bean
    @Profile("sbx")
    public ReactiveJwtDecoder jwtDecoderLocal() {
        return ReactiveJwtDecoders.fromIssuerLocation(authServerConfig.getJwtDecoderLocal());
    }

    @Bean
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
                        .pathMatchers(HttpMethod.POST, "/token/m2m").permitAll() //TODO Test purposes
                        .pathMatchers(HttpMethod.GET, "/token/verify").permitAll() //TODO Test purposes
                        .anyExchange().authenticated()
                ).csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer
                                .jwt(withDefaults()));
        return http.build();
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