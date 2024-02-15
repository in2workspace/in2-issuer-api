package es.in2.issuer.api.controller;

import es.in2.issuer.api.model.dto.CredentialOfferForPreAuthorizedCodeFlow;
import es.in2.issuer.api.exception.InvalidTokenException;
import es.in2.issuer.api.service.CredentialOfferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialOfferControllerTest {

    @Mock
    private CredentialOfferService credentialOfferService;

    @InjectMocks
    private CredentialOfferController controller;

    @Test
    void testCreateCredentialOfferV1_Success() {
        // Arrange
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String mockCredentialOfferUri = "https://www.example.com/credential-offer?credential_offer_uri=https://www.example.com/offer/123";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(credentialOfferService.createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null))
                .thenReturn(Mono.just(mockCredentialOfferUri));

        // Act
        Mono<String> result = controller.createCredentialOfferV1(null, mockExchange);

        // Assert
        result.subscribe(credentialOfferUri -> assertEquals(mockCredentialOfferUri, credentialOfferUri));
        verify(credentialOfferService, times(1)).createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null);
    }

    @Test
    void testCreateCredentialOfferV1_InvalidTokenException() {
        // Arrange
        String mockTokenString = "invalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        // Act
        Mono<String> result = controller.createCredentialOfferV1(null, mockExchange);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> {
                    assertTrue(error instanceof InvalidTokenException);
                    assertEquals("The request contains the wrong Access Token or the Access Token is missing", error.getMessage());
                }
        );
        verify(credentialOfferService, times(0)).createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null);
    }

    @Test
    void testCreateCredentialOfferV2_Success() {
        // Arrange
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String mockCredentialOfferUri = "https://www.example.com/credential-offer?credential_offer_uri=https://www.example.com/offer/456";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(credentialOfferService.createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null))
                .thenReturn(Mono.just(mockCredentialOfferUri));

        // Act
        Mono<String> result = controller.createCredentialOfferV2(mockExchange);

        // Assert
        result.subscribe(credentialOfferUri -> assertEquals(mockCredentialOfferUri, credentialOfferUri));
        verify(credentialOfferService, times(1)).createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null);
    }

    @Test
    void testCreateCredentialOfferV2_InvalidToken() {
        // Arrange
        String mockTokenString = "invalidToken";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        // Act
        Mono<String> result = controller.createCredentialOfferV2(mockExchange);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> {
                    assertTrue(error instanceof InvalidTokenException);
                    assertEquals("The request contains the wrong Access Token or the Access Token is missing", error.getMessage());
                }
        );
        verify(credentialOfferService, times(0)).createCredentialOfferUriForPreAuthorizedCodeFlow(mockTokenString, null);
    }

    @Test
    void testGetCredentialOffer_Success() {
        // Arrange
        String mockCredentialOfferId = "mockCredentialOfferId";
        CredentialOfferForPreAuthorizedCodeFlow mockCredentialOffer = new CredentialOfferForPreAuthorizedCodeFlow(/* initialize with expected data */);

        when(credentialOfferService.getCredentialOffer(mockCredentialOfferId)).thenReturn(Mono.just(mockCredentialOffer));

        // Act
        Mono<CredentialOfferForPreAuthorizedCodeFlow> result = controller.getCredentialOffer(mockCredentialOfferId);

        // Assert
        result.subscribe(credentialOffer -> assertEquals(mockCredentialOffer, credentialOffer));
        verify(credentialOfferService, times(1)).getCredentialOffer(mockCredentialOfferId);
    }
}
