package es.in2.issuer.backoffice.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backoffice.domain.model.dto.NonceValidationResponse;
import es.in2.issuer.shared.domain.service.JWTService;
import es.in2.issuer.backoffice.domain.service.impl.ProofValidationServiceImpl;
import es.in2.issuer.backoffice.infrastructure.config.AuthServerConfig;
import es.in2.issuer.backoffice.infrastructure.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProofValidationServiceImplTest {

    @Mock
    private AuthServerConfig authServerConfig;

    @Mock
    private WebClientConfig webClientConfig;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ProofValidationServiceImpl service;

    @Test
    void isProofValid_valid() throws JsonProcessingException {
        String validProof = "eyJraWQiOiJkaWQ6a2V5OnpEbmFlbURadmk2UFdMbjRLRjY2NlJzZ3ZTSnR5R1B4V05GQW8xenZNSmliTGFCSHYjekRuYWVtRFp2aTZQV0xuNEtGNjY2UnNndlNKdHlHUHhXTkZBbzF6dk1KaWJMYUJIdiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbURadmk2UFdMbjRLRjY2NlJzZ3ZTSnR5R1B4V05GQW8xenZNSmliTGFCSHYiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjMzMjE3NjMwOTgzLCJpYXQiOjE3MTMxNjY5ODMsIm5vbmNlIjoiLVNReklWbWxRTUNWd2xRak53SnRRUT09In0.hgLg04YCmEMa30JQYTZSz3vEGxTfBNYdx3A3wSNrtuJcb9p-96MtPCmLTpIFBU_CLTI4Wm4_lc-rbRMitIiOxA";
        String token = "token";
        when(jwtService.validateJwtSignatureReactive(any())).thenReturn(Mono.just(true));
        when(authServerConfig.getAuthServerNonceValidationPath()).thenReturn("nonce-validation-endpoint");
        // Ensure the JSON is valid and corresponds to a Grant object
        String jsonString = "{\"is_nonce_valid\":\"true\"}";
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        // Create a mock ClientResponse for a successful response
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(jsonString)
                .build();
        // Stub the exchange function to return the mock ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        when(webClientConfig.commonWebClient()).thenReturn(webClient);
        // Mock objectMapper to return a NonceValidationResponse
        NonceValidationResponse nonceValidationResponse = NonceValidationResponse.builder().isNonceValid(true).build();
        when(objectMapper.readValue(jsonString, NonceValidationResponse.class)).thenReturn(nonceValidationResponse);
        Mono<Boolean> result = service.isProofValid(validProof, token);
        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertTrue(response, "The response is wrong");
                })
                .verifyComplete();
    }

    @Test
    void isProofValid_notValid() throws JsonProcessingException {
        String notValidProof = "eyJraWQiOiJkaWQ6a2V5OnpEbmFlbURadmk2UFdMbjRLRjY2NlJzZ3ZTSnR5R1B4V05GQW8xenZNSmliTGFCSHYjekRuYWVtRFp2aTZQV0xuNEtGNjY2UnNndlNKdHlHUHhXTkZBbzF6dk1KaWJMYUJIdiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbURadmk2UFdMbjRLRjY2NlJzZ3ZTSnR5R1B4V05GQW8xenZNSmliTGFCSHYiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjMzMjE3NjMwOTgzLCJpYXQiOjE3MTMxNjY5ODMsIm5vbmNlIjoiLVNReklWbWxRTUNWd2xRak53SnRRUT09In0.hgLg04YCmEMa30JQYTZSz3vEGxTfBNYdx3A3wSNrtuJcb9p-96MtPCmLTpIFBU_CLTI4Wm4_lc-rbRMitIiOxA";
        String token = "token";
        when(jwtService.validateJwtSignatureReactive(any())).thenReturn(Mono.just(true));
        when(authServerConfig.getAuthServerNonceValidationPath()).thenReturn("nonce-validation-endpoint");
        // Ensure the JSON is valid and corresponds to a Grant object
        String jsonString = "{\"is_nonce_valid\":\"false\"}";
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        // Create a mock ClientResponse for a successful response
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(jsonString)
                .build();
        // Stub the exchange function to return the mock ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        when(webClientConfig.commonWebClient()).thenReturn(webClient);
        // Mock objectMapper to return a NonceValidationResponse
        NonceValidationResponse nonceValidationResponse = NonceValidationResponse.builder().isNonceValid(false).build();
        when(objectMapper.readValue(jsonString, NonceValidationResponse.class)).thenReturn(nonceValidationResponse);
        Mono<Boolean> result = service.isProofValid(notValidProof, token);
        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertFalse(response, "The response is wrong");
                })
                .verifyComplete();
    }

}