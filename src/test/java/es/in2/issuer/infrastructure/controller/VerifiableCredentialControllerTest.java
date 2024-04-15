package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.service.impl.VerifiableCredentialIssuanceServiceImpl;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.CredentialDefinition;
import es.in2.issuer.domain.model.CredentialRequest;
import es.in2.issuer.domain.model.Proof;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.infrastructure.controller.VerifiableCredentialController;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialControllerTest {

    @Mock
    private VerifiableCredentialIssuanceServiceImpl verifiableCredentialIssuanceService;

    @InjectMocks
    private VerifiableCredentialController controller;

    @Test
    void testCreateVerifiableCredential_Success() {
        // Arrange
        CredentialRequest mockCredentialRequest = new CredentialRequest("format",new CredentialDefinition(List.of("")), new Proof("proofType", "jwt"));
        //Example Token with claim "preferred_username" : "username"
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
        VerifiableCredentialResponse mockResponse = new VerifiableCredentialResponse("format", "credential", "nonce", 35);

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(verifiableCredentialIssuanceService.generateVerifiableCredentialResponse("username", mockCredentialRequest, mockTokenString))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<VerifiableCredentialResponse> result = controller.createVerifiableCredential(mockCredentialRequest, mockExchange);

        // Assert
        result.subscribe(response -> assertEquals(mockResponse, response));

        verify(verifiableCredentialIssuanceService, times(1))
                .generateVerifiableCredentialResponse("username", mockCredentialRequest, mockTokenString);
    }

    @Test
    void testCreateVerifiableCredential_InvalidTokenException() {
        // Arrange
        CredentialRequest mockCredentialRequest = new CredentialRequest("",new CredentialDefinition(List.of("")), new Proof("", ""));
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, URI.create("/example"))
                .build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        // Act
        Mono<VerifiableCredentialResponse> result = controller.createVerifiableCredential(mockCredentialRequest, mockExchange);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> {
                    assertTrue(error instanceof InvalidTokenException);
                    assertEquals("The request contains the wrong Access Token or the Access Token is missing", error.getMessage());
                }
        );
        verify(verifiableCredentialIssuanceService, times(0)).generateVerifiableCredentialResponse("username", mockCredentialRequest, "token");
    }
}
