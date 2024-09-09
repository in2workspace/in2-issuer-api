package es.in2.issuer.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.dto.UserDetails;
import es.in2.issuer.domain.service.impl.AccessTokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceImplTest {

    private static final String BEARER = "Bearer ";

    @Mock
    private SignedJWT mockSignedJwt;
    @InjectMocks
    private AccessTokenServiceImpl accessTokenServiceImpl;

    @Test
    void testGetCleanBearerToken_Valid() {
        String validHeader = "Bearer validToken123";
        Mono<String> result = accessTokenServiceImpl.getCleanBearerToken(validHeader);
        StepVerifier.create(result)
                .expectNext("validToken123")
                .verifyComplete();
    }

    @Test
    void testGetCleanBearerToken_Invalid() {
        String invalidHeader = "invalidToken123";
        Mono<String> result = accessTokenServiceImpl.getCleanBearerToken(invalidHeader);
        StepVerifier.create(result)
                .expectNext(invalidHeader)
                .verifyComplete();
    }

    @Test
    void testGetUserIdFromHeader_Valid() throws Exception {
        String validHeader = "Bearer token";
        String expectedUserId = "userId123";
        String jwtPayload = "{\"sub\":\"" + expectedUserId + "\"}";

        try (MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {
            SignedJWT signedJWT = mock(SignedJWT.class);
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString())).thenReturn(signedJWT);
            when(signedJWT.getPayload()).thenReturn(new Payload(jwtPayload));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(jwtPayload);
            when(signedJWT.getPayload()).thenReturn(new Payload(payloadJson.toString()));

            Mono<String> result = accessTokenServiceImpl.getUserId(validHeader);

            StepVerifier.create(result)
                    .expectNext(expectedUserId)
                    .verifyComplete();
        }
    }

    @Test
    void testGetUserIdFromHeader_ThrowsParseException() {
        String invalidHeader = "Bearer invalidToken";

        try (MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString()))
                    .thenThrow(new ParseException("Invalid token", 0));

            Mono<String> result = accessTokenServiceImpl.getUserId(invalidHeader);

            StepVerifier.create(result)
                    .expectError(ParseException.class)
                    .verify();
        }
    }

    @Test
    void testGetOrganizationId_ValidToken() {
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb25JZGVudGlmaWVyIjoib3JnMTIzIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String expectedOrganizationId = "org123";
        String jwtPayload = "{\"organizationIdentifier\":\"" + expectedOrganizationId + "\"}";

        try (MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString())).thenReturn(mockSignedJwt);
            when(mockSignedJwt.getPayload()).thenReturn(new Payload(jwtPayload));

            Mono<String> result = accessTokenServiceImpl.getOrganizationId("Bearer " + validJwtToken);

            StepVerifier.create(result)
                    .expectNext(expectedOrganizationId)
                    .verifyComplete();
        }
    }

    @Test
    void testGetOrganizationId_InvalidToken() {
        String invalidJwtToken = "invalid-jwt-token";

        try (MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {
            mockedJwtStatic.when(() -> SignedJWT.parse(anyString())).thenThrow(new ParseException("Invalid token", 0));

            Mono<String> result = accessTokenServiceImpl.getOrganizationId("Bearer " + invalidJwtToken);

            StepVerifier.create(result)
                    .expectError(ParseException.class)
                    .verify();
        }
    }

    @Test
    void testGetUserDetailsFromCurrentSession_ValidToken() throws ParseException {
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21tb25OYW1lIjoiQ29tbW9uIE5hbWUiLCJjb3VudHJ5IjoiQ291bnRyeSIsImVtYWlsQWRkcmVzcyI6ImVtYWlsQGV4YW1wbGUuY29tIiwic2VyaWFsTnVtYmVyIjoiU2VyaWFsTnVtYmVyIiwib3JnYW5pemF0aW9uSWRlbnRpZmllciI6Ik9yZ0lkZW50aWZpZXIiLCJvcmdhbml6YXRpb24iOiJPcmdhbml6YXRpb24ifQ.i9zpO0jkK36n03c9-6xNfenr5siYZ02baGxvfSto3Eg";
        UserDetails userDetails = UserDetails.builder()
                .commonName("Common Name")
                .country("Country")
                .emailAddress("email@example.com")
                .organization("Organization")
                .organizationIdentifier("OrgIdentifier")
                .serialNumber("SerialNumber").build();

        String jwtPayload = "{\"commonName\": \"Common Name\",\"country\": \"Country\",\"emailAddress\": \"email@example.com\",\"serialNumber\": \"SerialNumber\",\"organizationIdentifier\": \"OrgIdentifier\",\"organization\": \"Organization\"}";

        Jwt jwt = Jwt.withTokenValue(validJwtToken).header("alg", "HS256").claim("organizationIdentifier", "Organization").build();
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContext securityContext = new SecurityContextImpl(jwtAuthenticationToken);

        try (MockedStatic<ReactiveSecurityContextHolder> mockedContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
             MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {

            mockedContextHolder.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            // Create a JWSHeader and JWTClaimsSet from the payload
            JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse("{\"alg\":\"HS256\"}")).build();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwtPayload);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);

            mockedJwtStatic.when(() -> SignedJWT.parse(validJwtToken)).thenReturn(signedJWT);

            Mono<UserDetails> result = accessTokenServiceImpl.getUserDetailsFromCurrentSession();

            StepVerifier.create(result)
                    .expectNext(userDetails)
                    .verifyComplete();
        }
    }

    @Test
    void testGetOrganizationIdFromCurrentSession_ValidToken() throws ParseException {
        String validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJvcmdhbml6YXRpb25JZGVudGlmaWVyIjoib3JnMTIzIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String expectedOrganizationId = "org123";
        String jwtPayload = "{\"organizationIdentifier\":\"" + expectedOrganizationId + "\"}";

        Jwt jwt = Jwt.withTokenValue(validJwtToken).header("alg", "HS256").claim("organizationIdentifier", expectedOrganizationId).build();
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContext securityContext = new SecurityContextImpl(jwtAuthenticationToken);

        try (MockedStatic<ReactiveSecurityContextHolder> mockedContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
             MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {

            mockedContextHolder.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            // Create a JWSHeader and JWTClaimsSet from the payload
            JWSHeader jwsHeader = new JWSHeader.Builder(JWSHeader.parse("{\"alg\":\"HS256\"}")).build();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwtPayload);
            SignedJWT signedJWT = new SignedJWT(jwsHeader, jwtClaimsSet);

            mockedJwtStatic.when(() -> SignedJWT.parse(validJwtToken)).thenReturn(signedJWT);

            Mono<String> result = accessTokenServiceImpl.getOrganizationIdFromCurrentSession();

            StepVerifier.create(result)
                    .expectNext(expectedOrganizationId)
                    .verifyComplete();
        }
    }

    @Test
    void testGetOrganizationIdFromCurrentSession_InvalidToken() {
        String invalidJwtToken = "invalid-jwt-token";

        // Creamos un JWT con una reclamación mínima
        Jwt jwt = Jwt.withTokenValue(invalidJwtToken)
                .header("alg", "none")
                .claim("sub", "subject")
                .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContext securityContext = new SecurityContextImpl(jwtAuthenticationToken);

        try (MockedStatic<ReactiveSecurityContextHolder> mockedContextHolder = mockStatic(ReactiveSecurityContextHolder.class);
             MockedStatic<SignedJWT> mockedJwtStatic = mockStatic(SignedJWT.class)) {

            mockedContextHolder.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.just(securityContext));

            mockedJwtStatic.when(() -> SignedJWT.parse(invalidJwtToken)).thenThrow(new ParseException("Invalid token", 0));

            Mono<String> result = accessTokenServiceImpl.getOrganizationIdFromCurrentSession();

            StepVerifier.create(result)
                    .expectError(ParseException.class)
                    .verify();
        }
    }


    @Test
    void testGetOrganizationIdFromCurrentSession_EmptyToken() {
        try (MockedStatic<ReactiveSecurityContextHolder> mockedContextHolder = mockStatic(ReactiveSecurityContextHolder.class)) {
            mockedContextHolder.when(ReactiveSecurityContextHolder::getContext)
                    .thenReturn(Mono.empty());

            Mono<String> result = accessTokenServiceImpl.getOrganizationIdFromCurrentSession();

            StepVerifier.create(result)
                    .expectError(InvalidTokenException.class)
                    .verify();
        }
    }
}