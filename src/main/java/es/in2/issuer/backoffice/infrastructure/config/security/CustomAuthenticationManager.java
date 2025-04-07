package es.in2.issuer.backoffice.infrastructure.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backoffice.domain.service.VerifierService;
import es.in2.issuer.backoffice.infrastructure.config.AuthServerConfig;
import es.in2.issuer.backoffice.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
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
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    private final AuthServerConfig authServerConfig;
    private final VerifierConfig verifierConfig;
    private final VerifierService verifierService;
    private final ObjectMapper objectMapper;
    private final ReactiveJwtDecoder internalJwtDecoder;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        // Extract the payload without verifying the signature
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            log.warn("Token JWT con formato inválido");
            return Mono.error(new BadCredentialsException("Token JWT inválido"));
        }

        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Error al decodificar el payload del token JWT", e);
            return Mono.error(new BadCredentialsException("Token JWT malformado", e));
        }

        String issuer;
        try {
            JsonNode payloadNode = objectMapper.readTree(payload);
            issuer = payloadNode.get("iss").asText();
            log.debug("Emisor del token JWT: {}", issuer);
        } catch (Exception e) {
            log.error("Error al analizar el payload del token JWT", e);
            return Mono.error(new BadCredentialsException("Error al analizar el token JWT", e));
        }

        if (authServerConfig.getJwtValidator().equals(issuer)) {
            // Internal issuer (OAuth2 resource server)
            log.debug("Emisor interno detectado");
            return internalJwtDecoder.decode(token)
                    .map(jwt -> (Authentication) new JwtAuthenticationToken(jwt, Collections.emptyList()))
                    .doOnError(e -> log.error("Error al decodificar token interno", e));
        } else if (verifierConfig.getVerifierExternalDomain().equals(issuer)) {
            // External issuer (Verifier service)
            log.debug("Emisor externo detectado");
            return verifierService.verifyToken(token)
                    .then(parseExternalJwt(token))
                    .map(jwt -> (Authentication) new JwtAuthenticationToken(jwt, Collections.emptyList()))
                    .doOnError(e -> log.error("Error al validar token externo", e));
        } else {
            log.warn("Emisor desconocido: {}", issuer);
            return Mono.error(new BadCredentialsException("Emisor desconocido"));
        }
    }

    private Mono<Jwt> parseExternalJwt(String token) {
        return Mono.fromCallable(() -> {
            String[] parts = token.split("\\.");
            if (parts.length < 3) {
                throw new BadCredentialsException("Token JWT inválido");
            }

            // Decode and parse headers
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            Map<String, Object> headers = objectMapper.readValue(headerJson, Map.class);

            // Decode and parse the payload
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> claims = objectMapper.readValue(payloadJson, Map.class);

            // Extract issuedAt and expiresAt times if present
            Instant issuedAt = claims.containsKey("iat") ? Instant.ofEpochSecond(((Number) claims.get("iat")).longValue()) : Instant.now();
            Instant expiresAt = claims.containsKey("exp") ? Instant.ofEpochSecond(((Number) claims.get("exp")).longValue()) : Instant.now().plusSeconds(3600);

            return new Jwt(token, issuedAt, expiresAt, headers, claims);
        });
    }

}


