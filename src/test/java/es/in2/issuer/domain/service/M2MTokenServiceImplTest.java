package es.in2.issuer.domain.service;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.impl.M2MTokenServiceImpl;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.any;

import es.in2.issuer.infrastructure.config.VerifierConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@ExtendWith(MockitoExtension.class)
public class M2MTokenServiceImplTest {

    @Mock
    private WebClient oauth2VerifierWebClient;

    @Mock
    private VerifierConfig verifierConfig;

    @Mock
    private JWTService jwtService;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private M2MTokenServiceImpl m2MTokenService;

    @BeforeEach
    void setUp(){
        // Mock the WebClient behavior
        VerifierOauth2AccessToken mockToken = new VerifierOauth2AccessToken("mockAccessToken", "Bearer ","3600");
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String jwtBase64 = "ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnpkV0lpT2lJeE1qTTBOVFkzT0Rrd0lpd2libUZ0WlNJNklrcHZhRzRnUkc5bElpd2lhV0YwSWpveE5URTJNak01TURJeWZRLlNmbEt4d1JKU01lS0tGMlFUNGZ3cE1lSmYzNlBPazZ5SlZfYWRRc3N3NWM=";

        // Mock the WebClient behavior to simulate an error
        when(oauth2VerifierWebClient.post()).thenReturn(requestBodyUriSpec);

        // Mock the Verifier config behavior
        when(verifierConfig.getVerifierPathsTokenPath()).thenReturn("/token/path");
        when(verifierConfig.getVerifierVc()).thenReturn(jwtBase64);
        when(verifierConfig.getVerifierClientAssertionTokenExpiration()).thenReturn("30");
        when(verifierConfig.getVerifierClientAssertionTokenCronUnit()).thenReturn("DAYS");
        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-domain");
        when(verifierConfig.getCredentialSubjectKey()).thenReturn("credential-subject-key");

        // Setup WebClient request mocking
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(BodyInserters.fromFormData("name", "value")))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(VerifierOauth2AccessToken.class))
                .thenReturn(Mono.error(new RuntimeException("Error fetching token")));

        // Mock JWT parsing and generation
        SignedJWT mockSignedJWT = mock(SignedJWT.class);
        Payload mockPayload = mock(Payload.class);

        when(jwtService.parseJWT(jwt)).thenReturn(mockSignedJWT);
        when(jwtService.getPayloadFromSignedJWT(mockSignedJWT)).thenReturn(mockPayload);
        when(jwtService.getClaimFromPayload(mockPayload, "sub")).thenReturn("subject");

        // Capture the argument passed to generateJWT
        ArgumentCaptor<String> jwtPayloadCaptor = ArgumentCaptor.forClass(String.class);
        when(jwtService.generateJWT(jwtPayloadCaptor.capture())).thenReturn(jwt);

    }

    @Test
    public void testGetM2MToken_Success() {
//        // Mock the WebClient behavior
//        VerifierOauth2AccessToken mockToken = new VerifierOauth2AccessToken("mockAccessToken", "Bearer ","3600");
//        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
//        String jwtBase64 = "ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnpkV0lpT2lJeE1qTTBOVFkzT0Rrd0lpd2libUZ0WlNJNklrcHZhRzRnUkc5bElpd2lhV0YwSWpveE5URTJNak01TURJeWZRLlNmbEt4d1JKU01lS0tGMlFUNGZ3cE1lSmYzNlBPazZ5SlZfYWRRc3N3NWM=";
//
//        when(oauth2VerifierWebClient.post()).thenReturn(requestBodyUriSpec);
//
//        when(verifierConfig.getVerifierPathsTokenPath()).thenReturn("/token/path");
//        when(verifierConfig.getVerifierVc()).thenReturn(jwtBase64);
//        when(verifierConfig.getVerifierClientAssertionTokenExpiration()).thenReturn("30");
//        when(verifierConfig.getVerifierClientAssertionTokenCronUnit()).thenReturn("DAYS");
//        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-domain");
//
//        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.body(BodyInserters.fromFormData("name", "value")))
//                .thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(VerifierOauth2AccessToken.class)).thenReturn(Mono.just(mockToken));
//
//        // Mock JWT parsing and generation
//        SignedJWT mockSignedJWT = mock(SignedJWT.class);
//        Payload mockPayload = mock(Payload.class);
//
//        when(jwtService.parseJWT(jwt)).thenReturn(mockSignedJWT);
//        when(jwtService.getPayloadFromSignedJWT(mockSignedJWT)).thenReturn(mockPayload);
//        when(jwtService.getClaimFromPayload(mockPayload, "sub")).thenReturn("subject");
//
//        // Capture the argument passed to generateJWT
//        ArgumentCaptor<String> jwtPayloadCaptor = ArgumentCaptor.forClass(String.class);
//        when(jwtService.generateJWT(jwtPayloadCaptor.capture())).thenReturn(jwt);

        Mono<VerifierOauth2AccessToken> result = m2MTokenService.getM2MToken();

        StepVerifier.create(result)
                .expectNextMatches(token -> token.accessToken().equals("mockAccessToken"))
                .verifyComplete();
    }

    @Test
    public void testGetM2MToken_Error() {

//        VerifierOauth2AccessToken mockToken = new VerifierOauth2AccessToken("mockAccessToken", "Bearer ","3600");
//        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
//        String jwtBase64 = "ZXlKaGJHY2lPaUpJVXpJMU5pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SnpkV0lpT2lJeE1qTTBOVFkzT0Rrd0lpd2libUZ0WlNJNklrcHZhRzRnUkc5bElpd2lhV0YwSWpveE5URTJNak01TURJeWZRLlNmbEt4d1JKU01lS0tGMlFUNGZ3cE1lSmYzNlBPazZ5SlZfYWRRc3N3NWM=";
//
//        // Mock the WebClient behavior to simulate an error
//        when(oauth2VerifierWebClient.post()).thenReturn(requestBodyUriSpec);
//
//        // Mock the Verifier config behavior
//        when(verifierConfig.getVerifierPathsTokenPath()).thenReturn("/token/path");
//        when(verifierConfig.getVerifierVc()).thenReturn(jwtBase64);
//        when(verifierConfig.getVerifierClientAssertionTokenExpiration()).thenReturn("30");
//        when(verifierConfig.getVerifierClientAssertionTokenCronUnit()).thenReturn("DAYS");
//        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-domain");
//        when(verifierConfig.getCredentialSubjectKey()).thenReturn("credential-subject-key");
//
//        // Setup WebClient request mocking
//        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.body(BodyInserters.fromFormData("name", "value")))
//                .thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(VerifierOauth2AccessToken.class))
//                .thenReturn(Mono.error(new RuntimeException("Error fetching token")));
//
//        // Mock JWT parsing and generation
//        SignedJWT mockSignedJWT = mock(SignedJWT.class);
//        Payload mockPayload = mock(Payload.class);
//
//        when(jwtService.parseJWT(jwt)).thenReturn(mockSignedJWT);
//        when(jwtService.getPayloadFromSignedJWT(mockSignedJWT)).thenReturn(mockPayload);
//        when(jwtService.getClaimFromPayload(mockPayload, "sub")).thenReturn("subject");
//
//        // Capture the argument passed to generateJWT
//        ArgumentCaptor<String> jwtPayloadCaptor = ArgumentCaptor.forClass(String.class);
//        when(jwtService.generateJWT(jwtPayloadCaptor.capture())).thenReturn(jwt);

        // Test the method
        Mono<VerifierOauth2AccessToken> result = m2MTokenService.getM2MToken();

        // Verify the error scenario using StepVerifier
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Error fetching token"))
                .verify();
    }

}
