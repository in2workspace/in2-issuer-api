package es.in2.issuer.api.controller;

import es.in2.issuer.api.model.dto.CredentialRequestDTO;
import es.in2.issuer.api.model.dto.ProofDTO;
import es.in2.issuer.api.model.dto.VerifiableCredentialResponseDTO;
import es.in2.issuer.api.exception.InvalidTokenException;
import es.in2.issuer.api.service.VerifiableCredentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialControllerTest {

    @Mock
    private VerifiableCredentialService verifiableCredentialService;

    @InjectMocks
    private VerifiableCredentialController controller;

    @Test
    void testCreateVerifiableCredential_Success() {
        // Arrange
        CredentialRequestDTO mockCredentialRequest = new CredentialRequestDTO("format",new ProofDTO("proofType","jwt"));
        //Example Token with claim "preferred_username" : "username"
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
        VerifiableCredentialResponseDTO mockResponse = new VerifiableCredentialResponseDTO(new ArrayList<>());

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(verifiableCredentialService.generateVerifiableCredentialResponse("username", mockCredentialRequest, mockTokenString))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<VerifiableCredentialResponseDTO> result = controller.createVerifiableCredential(mockCredentialRequest, mockExchange);

        // Assert
        result.subscribe(response -> assertEquals(mockResponse, response));

        verify(verifiableCredentialService, times(1))
                .generateVerifiableCredentialResponse("username", mockCredentialRequest, mockTokenString);
    }

    @Test
    void testCreateVerifiableCredential_InvalidTokenException() {
        // Arrange
        CredentialRequestDTO mockCredentialRequest = new CredentialRequestDTO("",new ProofDTO("",""));
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, URI.create("/example"))
                .build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        // Act
        Mono<VerifiableCredentialResponseDTO> result = controller.createVerifiableCredential(mockCredentialRequest, mockExchange);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> {
                    assertTrue(error instanceof InvalidTokenException);
                    assertEquals("The request contains the wrong Access Token or the Access Token is missing", error.getMessage());
                }
        );
        verify(verifiableCredentialService, times(0)).generateVerifiableCredentialResponse("username", mockCredentialRequest, "token");
    }

    @Test
    void testGetVerifiableCredential_Success() {
        // Arrange
        String mockCredentialId = "mockCredentialId";
        String mockLocation = "http://example.com/mockCredential";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, URI.create("/example"))
                .build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(verifiableCredentialService.getVerifiableCredential(mockCredentialId)).thenReturn(Mono.just(mockLocation));

        // Act
        Mono<Void> result = controller.getVerifiableCredential(mockCredentialId, mockExchange);

        // Assert
        result.subscribe(
                response -> {
                    assertEquals(HttpStatus.OK, mockExchange.getResponse().getStatusCode());
                    assertEquals(mockLocation, mockExchange.getResponse().getHeaders().getFirst(HttpHeaders.LOCATION));
                }
        );
        verify(verifiableCredentialService, times(1)).getVerifiableCredential(mockCredentialId);
    }
}
