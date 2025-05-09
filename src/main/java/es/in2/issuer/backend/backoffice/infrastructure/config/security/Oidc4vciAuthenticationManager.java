package es.in2.issuer.backend.backoffice.infrastructure.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import es.in2.issuer.backend.shared.domain.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class Oidc4vciAuthenticationManager implements ReactiveAuthenticationManager {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        log.info("Validating JWT token");
        String token = authentication.getCredentials().toString();

        return Mono.fromCallable(() -> JWSObject.parse(token))
                .flatMap(jwsObject -> jwtService.validateJwtSignatureReactive(jwsObject)
                        .flatMap(isValid -> {
                            if (Boolean.TRUE.equals(isValid)) {
                                return parseJwt(token)
                                        .map(jwt -> (Authentication) new JwtAuthenticationToken(jwt, Collections.emptyList()));
                            } else {
                                return Mono.error(new BadCredentialsException("Invalid JWT token"));
                            }
                        }));
    }

    private Mono<Jwt> parseJwt(String token) {
        return Mono.fromCallable(() -> {
            String[] parts = token.split("\\.");

            // Decode and parse headers
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            Map<String, Object> headers = objectMapper.readValue(headerJson, Map.class);

            // Decode and parse payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            // Extract issuedAt and expiresAt times if present
            Instant issuedAt = claims.containsKey("iat") ? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue()) : Instant.now();
            Instant expiresAt = claims.containsKey("exp") ? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue()) : Instant.now().plusSeconds(3600);

            return new Jwt(token, issuedAt, expiresAt, headers, claims);
        });
    }
}