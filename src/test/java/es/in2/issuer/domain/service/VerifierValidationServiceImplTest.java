package es.in2.issuer.domain.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.service.impl.VerifierValidationServiceImpl;

import es.in2.issuer.infrastructure.config.VerifierConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class VerifierValidationServiceImplTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private VerifierConfig verifierConfig;

    @InjectMocks
    private VerifierValidationServiceImpl m2MTokenService;


    @Test
    void testVerifyToken_validToken() {

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaXNzIjoiaXNzdWVyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.ZfZD0XwVq6wUi6csEKQ2tXKxguoaDMWrapvOf04h890";
        SignedJWT signedToken = mock(SignedJWT.class);
        Payload payload = mock(Payload.class);

        when(verifierConfig.getVerifierDidKey()).thenReturn("issuer");

        when(jwtService.parseJWT(jwtToken)).thenReturn(signedToken);
        when(jwtService.getPayloadFromSignedJWT(signedToken)).thenReturn(payload);
        when(jwtService.getClaimFromPayload(payload,"iss")).thenReturn("issuer");
        when(jwtService.getExpirationFromToken(jwtToken)).thenReturn(32496025441L);
        when(jwtService.validateJwtSignatureReactive(any(JWSObject.class))).thenReturn(Mono.just(true));

        Mono<Void> result = m2MTokenService.verifyToken(jwtToken);

        StepVerifier.create(result)
                .verifyComplete();
    }


    @Test
    void testVerifyM2MToken_validToken_but_issuer_did_key_not_equal_verifier_did_key() {

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaXNzIjoiaXNzdWVyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.ZfZD0XwVq6wUi6csEKQ2tXKxguoaDMWrapvOf04h890";
        SignedJWT signedToken = mock(SignedJWT.class);
        Payload payload = mock(Payload.class);

        when(verifierConfig.getVerifierDidKey()).thenReturn("verifier-did-key-not-equal-issuer");

        when(jwtService.parseJWT(jwtToken)).thenReturn(signedToken);
        when(jwtService.getPayloadFromSignedJWT(signedToken)).thenReturn(payload);
        when(jwtService.getClaimFromPayload(payload,"iss")).thenReturn("issuer");
        when(jwtService.getExpirationFromToken(jwtToken)).thenReturn(32496025441L);
        when(jwtService.validateJwtSignatureReactive(any(JWSObject.class))).thenReturn(Mono.just(true));

        Mono<Void> result = m2MTokenService.verifyToken(jwtToken);

        StepVerifier.create(result)
                .expectError(JWTVerificationException.class)
                .verify();
    }

}
