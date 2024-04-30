package es.in2.issuer.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.domain.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialManagementControllerTest {
    @Mock
    private CredentialManagementService credentialManagementService;
    @Mock
    private VerifiableCredentialService verifiableCredentialService;
    @Mock
    private HttpUtils httpUtils;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private CredentialManagementController controller;

    private WebTestClient webTestClient;


    @Test
    void testGetCredential(){
        // Arrange
        UUID credentialId = UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505");
        Timestamp credentialModifiedAt = Timestamp.valueOf("2024-04-29 16:41:02.565");
        Map<String, Object> credential = new HashMap<>();
        credential.put("key1", "value1");

        CredentialRequest mockCredentialRequest = new CredentialRequest("format",new CredentialDefinition(List.of("")), new Proof("proofType", "jwt"));
        //Example Token with claim "sub" : "1234567890"
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
        CredentialItem mockResponse = new CredentialItem(UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505"), credential, "jwt_vc_json", "VALID", credentialModifiedAt);

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, URI.create("/example"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(credentialManagementService.getCredential(credentialId, "1234567890"))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<CredentialItem> result = controller.getCredential(credentialId, mockExchange);

        // Assert
        result.subscribe(response -> assertEquals(mockResponse, response));

        verify(credentialManagementService, times(1))
                .getCredential(credentialId, "1234567890");
    }

    @Test
    void testGetCredentials() {
        // Arrange
        UUID credentialId1 = UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505");
        UUID credentialId2 = UUID.fromString("a1987bc9-08f7-4b93-9177-67897b123456");
        Timestamp timestamp = Timestamp.valueOf("2024-04-29 16:41:02.565");
        Map<String, Object> credentialDetails = new HashMap<>();
        credentialDetails.put("key1", "value1");

        CredentialItem credentialItem1 = new CredentialItem(credentialId1, credentialDetails, "jwt_vc_json", "VALID", timestamp);
        CredentialItem credentialItem2 = new CredentialItem(credentialId2, credentialDetails, "jwt_vc_json", "VALID", timestamp);

        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, URI.create("/api/credentials"))
                .header("Authorization","Bearer "+mockTokenString).build();
        ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

        when(credentialManagementService.getCredentials("1234567890", 0, 10, "modifiedAt", Sort.Direction.DESC))
                .thenReturn(Flux.just(credentialItem1, credentialItem2));

        // Act
        Flux<CredentialItem> result = controller.getCredentials(mockExchange, 0, 10, "modifiedAt", Sort.Direction.DESC);

        // Assert
        StepVerifier.create(result)
                .expectNext(credentialItem1)
                .expectNext(credentialItem2)
                .verifyComplete();

        verify(credentialManagementService, times(1))
                .getCredentials("1234567890", 0, 10, "modifiedAt", Sort.Direction.DESC);
    }

    @Test
    void testSignVerifiableCredentialsSuccess() throws Exception {
        // Setup for static mocking (Mockito 3.4+ required)
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            // Arrange
            String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
            String unsignedCredential = "{}";
            UUID credentialId = UUID.randomUUID();
            String signedCredential = "signedCredentialExample";
            String jsonResponse = "{\"data\":\"" + signedCredential + "\"}";

            MockServerHttpRequest request = MockServerHttpRequest.post("/api/credentials/sign/" + credentialId)
                    .header("Authorization", "Bearer " + mockTokenString)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(unsignedCredential);
            ServerWebExchange mockExchange = MockServerWebExchange.builder(request).build();

            // Assuming Utils.getToken returns a mocked SignedJWT
            SignedJWT mockToken = SignedJWT.parse(mockTokenString);
            mockedUtils.when(() -> Utils.getToken(mockExchange)).thenReturn(mockToken);

            when(verifiableCredentialService.generateDeferredVcPayLoad(any()))
                    .thenReturn(Mono.just("vcPayload"));
            when(httpUtils.postRequest(any(), any(), any()))
                    .thenReturn(Mono.just(jsonResponse));
            when(credentialManagementService.updateCredential(any(), any(), any()))
                    .thenReturn(Mono.empty());

            // Act
            Mono<Void> result = controller.signVerifiableCredentials(credentialId, unsignedCredential, mockExchange);

            // Assert
            StepVerifier.create(result)
                    .expectSubscription()
                    .verifyComplete();

            verify(verifiableCredentialService).generateDeferredVcPayLoad(anyString());
            verify(httpUtils).postRequest(eq("http://localhost:8050/api/v1/signature/sign"), any(), anyString());
            verify(credentialManagementService).updateCredential(eq(signedCredential), eq(credentialId), anyString());
        }
    }
}