package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.CredentialOfferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialOfferControllerTest {

    @Mock
    private CredentialOfferService credentialOfferService;

    @Mock
    private CredentialOfferIssuanceService credentialOfferIssuanceService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private CredentialOfferController controller;

    @Test
    void testCreateCredentialOfferV1_Success() {
        // Arrange
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String mockCredentialOfferUri = "https://www.example.com/credential-offer?credential_offer_uri=https://www.example.com/offer/123";

        when(accessTokenService.getCleanBearerToken(any())).thenReturn(Mono.just(mockTokenString));
        when(credentialOfferIssuanceService.buildCredentialOfferUri(mockTokenString, null))
                .thenReturn(Mono.just(mockCredentialOfferUri));

        // Act
        Mono<String> result = controller.buildCredentialOffer("Bearer "+mockTokenString, null);

        // Assert
        result.subscribe(credentialOfferUri -> assertEquals(mockCredentialOfferUri, credentialOfferUri));
        verify(credentialOfferIssuanceService, times(1)).buildCredentialOfferUri(mockTokenString, null);
    }

    @Test
    void testGetCredentialOffer_Success() {
        // Arrange
        String mockCredentialOfferId = "mockCredentialOfferId";
        CustomCredentialOffer mockCredentialOffer = CustomCredentialOffer.builder().build();

        when(credentialOfferIssuanceService.getCustomCredentialOffer(mockCredentialOfferId)).thenReturn(Mono.just(mockCredentialOffer));

        // Mock
        ServerWebExchange mockExchange = mock(ServerWebExchange.class);
        ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
        when(mockExchange.getResponse()).thenReturn(mockResponse);
        HttpHeaders mockHeaders = new HttpHeaders();
        when(mockResponse.getHeaders()).thenReturn(mockHeaders);

        // Act
        Mono<CustomCredentialOffer> result = controller.getCredentialOffer(mockCredentialOfferId, mockExchange);

        // Assert
        result.subscribe(credentialOffer -> assertEquals(mockCredentialOffer, credentialOffer));
        verify(credentialOfferIssuanceService, times(1)).getCustomCredentialOffer(mockCredentialOfferId);
    }
}
