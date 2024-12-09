package es.in2.issuer.infrastructure.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.service.VerifierService;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationManagerTest {

    @Mock
    private AuthServerConfig authServerConfig;

    @Mock
    private VerifierConfig verifierConfig;

    @Mock
    private VerifierService verifierService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReactiveJwtDecoder internalJwtDecoder;

    @InjectMocks
    private CustomAuthenticationManager authenticationManager;

    // Helper method to base64url encode a string
    private String base64UrlEncode(String str) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void authenticate_withValidExternalToken_returnsAuthentication() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{\"iss\":\"external-issuer\",\"iat\":1633036800,\"exp\":1633040400}";
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        // Mock configurations
        when(authServerConfig.getJwtValidator()).thenReturn("internal-issuer");
        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-issuer");
        when(verifierService.verifyToken(token)).thenReturn(Mono.empty());

        // Use a real ObjectMapper to parse JSON
        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);
        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);

        Map<String, Object> headerMap = realObjectMapper.readValue(headerJson, Map.class);
        Map<String, Object> payloadMap = realObjectMapper.readValue(payloadJson, Map.class);

        when(objectMapper.readValue(headerJson, Map.class)).thenReturn(headerMap);
        when(objectMapper.readValue(payloadJson, Map.class)).thenReturn(payloadMap);

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(JwtAuthenticationToken.class::isInstance)
                .verifyComplete();

        verify(verifierService).verifyToken(token);
    }


    @Test
    void authenticate_withValidInternalToken_returnsAuthentication() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{\"iss\":\"internal-issuer\",\"iat\":1633036800,\"exp\":1633040400}";
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        Jwt decodedJwt = mock(Jwt.class);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);

        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);
        when(authServerConfig.getJwtValidator()).thenReturn("internal-issuer");
        when(internalJwtDecoder.decode(token)).thenReturn(Mono.just(decodedJwt));

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(JwtAuthenticationToken.class::isInstance)
                .verifyComplete();

        verify(internalJwtDecoder).decode(token);
    }

    @Test
    void authenticate_withInvalidTokenFormat_throwsBadCredentialsException() {
        // Arrange
        String token = "invalidtoken";
        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BadCredentialsException && e.getMessage().equals("Token JWT inválido"))
                .verify();
    }

    @Test
    void authenticate_withInvalidPayloadDecoding_throwsBadCredentialsException() {
        // Arrange
        String header = base64UrlEncode("{\"alg\":\"none\"}");
        String payload = "ñ";
        String token = header + "." + payload + ".signature";
        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BadCredentialsException && e.getMessage().startsWith("Token JWT malformado"))
                .verify();
    }

    @Test
    void authenticate_withUnknownIssuer_throwsBadCredentialsException() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{\"iss\":\"unknown-issuer\"}";
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);

        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);

        when(authServerConfig.getJwtValidator()).thenReturn("internal-issuer");
        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-issuer");

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BadCredentialsException && e.getMessage().equals("Emisor desconocido"))
                .verify();
    }

    @Test
    void authenticate_withInvalidInternalTokenSignature_throwsBadCredentialsException() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{\"iss\":\"internal-issuer\"}";
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);

        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);
        when(authServerConfig.getJwtValidator()).thenReturn("internal-issuer");
        when(internalJwtDecoder.decode(token)).thenReturn(Mono.error(new JwtException("Invalid signature")));

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectError(JwtException.class)
                .verify();

        verify(internalJwtDecoder).decode(token);
    }

    @Test
    void authenticate_withInvalidExternalTokenVerification_throwsBadCredentialsException() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{\"iss\":\"external-issuer\"}";
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);

        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);
        when(authServerConfig.getJwtValidator()).thenReturn("internal-issuer");
        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-issuer");
        when(verifierService.verifyToken(token)).thenReturn(Mono.error(new JWTVerificationException("Verification failed")));

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectError(JWTVerificationException.class)
                .verify();

        verify(verifierService).verifyToken(token);
    }

    @Test
    void authenticate_withTokenMissingIssuer_throwsBadCredentialsException() throws Exception {
        // Arrange
        String headerJson = "{\"alg\":\"none\"}";
        String payloadJson = "{}"; // Missing "iss"
        String header = base64UrlEncode(headerJson);
        String payload = base64UrlEncode(payloadJson);
        String token = header + "." + payload + ".signature";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode payloadNode = realObjectMapper.readTree(payloadJson);

        when(objectMapper.readTree(payloadJson)).thenReturn(payloadNode);

        Authentication authentication = new TestingAuthenticationToken(null, token);

        // Act
        Mono<Authentication> result = authenticationManager.authenticate(authentication);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof BadCredentialsException && e.getMessage().startsWith("Error al analizar el token JWT"))
                .verify();
    }

}

