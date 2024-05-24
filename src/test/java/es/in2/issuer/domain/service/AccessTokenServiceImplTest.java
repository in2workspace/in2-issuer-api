package es.in2.issuer.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.service.impl.AccessTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTokenServiceImplTest {

    private static final String BEARER = "Bearer ";
    private final AccessTokenServiceImpl service = new AccessTokenServiceImpl();

    @Test
    void testGetCleanBearerToken_Valid() {
        String validHeader = "Bearer validToken123";
        Mono<String> result = service.getCleanBearerToken(validHeader);
        StepVerifier.create(result)
                .expectNext("validToken123")
                .verifyComplete();
    }

    @Test
    void testGetCleanBearerToken_Invalid() {
        String invalidHeader = "invalidToken123";
        Mono<String> result = service.getCleanBearerToken(invalidHeader);
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testGetUserIdFromHeader_Valid() throws Exception {
        String validHeader = "Bearer token";
        String expectedUserId = "userId123";
        String jwtPayload = "{\"sub\":\"" + expectedUserId + "\"}";

        try (MockedStatic<SignedJWT> mockedJwtStatic = Mockito.mockStatic(SignedJWT.class)) {
            SignedJWT mockSignedJwt = mock(SignedJWT.class);
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString())).thenReturn(mockSignedJwt);
            when(mockSignedJwt.getPayload()).thenReturn(new Payload(jwtPayload));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(jwtPayload);
            when(mockSignedJwt.getPayload()).thenReturn(new Payload(payloadJson.toString()));

            Mono<String> result = service.getUserIdFromHeader(validHeader);

            StepVerifier.create(result)
                    .expectNext(expectedUserId)
                    .verifyComplete();
        }
    }

    @Test
    void testGetUserIdFromHeader_ThrowsParseException() {
        String invalidHeader = "Bearer invalidToken";

        try (MockedStatic<SignedJWT> mockedJwtStatic = Mockito.mockStatic(SignedJWT.class)) {
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString()))
                    .thenThrow(new ParseException("Invalid token", 0));

            Mono<String> result = service.getUserIdFromHeader(invalidHeader);

            StepVerifier.create(result)
                    .expectError(ParseException.class)
                    .verify();
        }
    }
}