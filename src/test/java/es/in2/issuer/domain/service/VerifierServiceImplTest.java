package es.in2.issuer.domain.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.domain.service.impl.VerifierServiceImpl;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VerifierServiceImplTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private VerifierConfig verifierConfig;

    @InjectMocks
    private VerifierServiceImpl verifierService;


    @Test
    void testVerifyToken_validToken() {

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaXNzIjoiaXNzdWVyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.ZfZD0XwVq6wUi6csEKQ2tXKxguoaDMWrapvOf04h890";
        SignedJWT signedToken = mock(SignedJWT.class);
        Payload payload = mock(Payload.class);

        when(verifierConfig.getDidKey()).thenReturn("issuer");

        when(jwtService.parseJWT(jwtToken)).thenReturn(signedToken);
        when(jwtService.getPayloadFromSignedJWT(signedToken)).thenReturn(payload);
        when(jwtService.getClaimFromPayload(payload,"iss")).thenReturn("issuer");
        when(jwtService.getExpirationFromToken(jwtToken)).thenReturn(32496025441L);
        when(jwtService.validateJwtSignatureReactive(any(JWSObject.class))).thenReturn(Mono.just(true));

        Mono<Void> result = verifierService.verifyToken(jwtToken);

        StepVerifier.create(result)
                .verifyComplete();
    }


    @Test
    void testVerifyM2MToken_validToken_but_issuer_did_key_not_equal_verifier_did_key() {

        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaXNzIjoiaXNzdWVyIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.ZfZD0XwVq6wUi6csEKQ2tXKxguoaDMWrapvOf04h890";
        SignedJWT signedToken = mock(SignedJWT.class);
        Payload payload = mock(Payload.class);

        when(verifierConfig.getDidKey()).thenReturn("verifier-did-key-not-equal-issuer");

        when(jwtService.parseJWT(jwtToken)).thenReturn(signedToken);
        when(jwtService.getPayloadFromSignedJWT(signedToken)).thenReturn(payload);
        when(jwtService.getClaimFromPayload(payload,"iss")).thenReturn("issuer");
        when(jwtService.getExpirationFromToken(jwtToken)).thenReturn(32496025441L);
        when(jwtService.validateJwtSignatureReactive(any(JWSObject.class))).thenReturn(Mono.just(true));

        Mono<Void> result = verifierService.verifyToken(jwtToken);

        StepVerifier.create(result)
                .expectError(JWTVerificationException.class)
                .verify();
    }

    @Test
    void getWellKnownInfo_shouldReturnOpenIDProviderMetadata() {
        // Arrange
        String verifierExternalDomain = "https://verifier.example.com";
        String verifierWellKnownPath = "/.well-known/openid-configuration";
        String wellKnownInfoEndpoint = verifierExternalDomain + verifierWellKnownPath;

        when(verifierConfig.getVerifierExternalDomain()).thenReturn(verifierExternalDomain);
        when(verifierConfig.getVerifierWellKnownPath()).thenReturn(verifierWellKnownPath);

        OpenIDProviderMetadata metadata = OpenIDProviderMetadata.builder()
                .issuer("https://verifier.example.com")
                .authorizationEndpoint("https://verifier.example.com/authorize")
                .tokenEndpoint("https://verifier.example.com/token")
                .build();

        // Mock the ExchangeFunction
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        // Create a ClientResponse with the desired body
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .body("{\"issuer\":\"https://verifier.example.com\",\"authorization_endpoint\":\"https://verifier.example.com/authorize\",\"token_endpoint\":\"https://verifier.example.com/token\"}")
                .build();

        // Configure the ExchangeFunction to return the mocked ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));

        // Create a WebClient with the mocked ExchangeFunction
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        // Inject the WebClient into the service
        verifierService = new VerifierServiceImpl(verifierConfig, jwtService, webClient);

        // Act
        Mono<OpenIDProviderMetadata> result = verifierService.getWellKnownInfo();

        // Assert
        StepVerifier.create(result)
                .expectNext(metadata)
                .verifyComplete();

        // Verify interactions
        verify(verifierConfig).getVerifierExternalDomain();
        verify(verifierConfig).getVerifierWellKnownPath();
        verifyNoMoreInteractions(verifierConfig);

        // Capture the request made
        ArgumentCaptor<ClientRequest> requestCaptor = ArgumentCaptor.forClass(ClientRequest.class);
        verify(exchangeFunction).exchange(requestCaptor.capture());

        ClientRequest capturedRequest = requestCaptor.getValue();

        // Verify that the request was made to the correct endpoint
        assertEquals(wellKnownInfoEndpoint, capturedRequest.url().toString());

        // Verify that a GET request was made
        assertEquals(HttpMethod.GET, capturedRequest.method());
    }

}
