package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.backend.domain.service.impl.VerifierServiceImpl;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
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

import static es.in2.issuer.backend.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.backend.domain.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class VerifierServiceImplTest {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private VerifierServiceImpl verifierService;

    @Test
    void getWellKnownInfo_shouldReturnOpenIDProviderMetadata() {
        // Arrange
        String verifierExternalUrl = "https://verifier.example.com";
        String verifierWellKnownPath = "/.well-known/openid-configuration";
        String wellKnownInfoEndpoint = verifierExternalUrl + verifierWellKnownPath;

        when(appConfig.getVerifierUrl()).thenReturn(verifierExternalUrl);

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
        verifierService = new VerifierServiceImpl(appConfig, webClient);

        // Act
        Mono<OpenIDProviderMetadata> result = verifierService.getWellKnownInfo();

        // Assert
        StepVerifier.create(result)
                .expectNext(metadata)
                .verifyComplete();

        // Verify interactions
        verify(appConfig).getVerifierUrl();
        verifyNoMoreInteractions(appConfig);

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
