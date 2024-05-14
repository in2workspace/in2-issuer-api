package es.in2.issuer.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.domain.util.Utils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.HashMap;
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
    @Mock
    private AppConfiguration appConfiguration;
    @Mock
    private AccessTokenService accessTokenService;
    @InjectMocks
    private CredentialManagementController controller;

    @Test
    void testGetCredential(){
        // Arrange
        UUID credentialId = UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505");
        Timestamp credentialModifiedAt = Timestamp.valueOf("2024-04-29 16:41:02.565");
        Map<String, Object> credential = new HashMap<>();
        credential.put("key1", "value1");

        //Example Token with claim "sub" : "1234567890"
        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
        CredentialItem mockResponse = new CredentialItem(UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505"), credential, "jwt_vc_json", "VALID", credentialModifiedAt);

        when(accessTokenService.getCleanBearerToken(any())).thenReturn(Mono.just(mockTokenString));
        when(accessTokenService.getUserIdFromHeader(any())).thenReturn(Mono.just("1234567890"));
        when(credentialManagementService.getCredential(credentialId, "1234567890"))
                .thenReturn(Mono.just(mockResponse));

        // Act
        Mono<CredentialItem> result = controller.getCredential("Bearer "+mockTokenString, credentialId);

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

        when(accessTokenService.getUserIdFromHeader(any())).thenReturn(Mono.just("1234567890"));
        when(credentialManagementService.getCredentials("1234567890", 0, 10, "modifiedAt", Sort.Direction.DESC))
                .thenReturn(Flux.just(credentialItem1, credentialItem2));

        // Act
        Flux<CredentialItem> result = controller.getCredentials("Bearer "+mockTokenString, 0, 10, "modifiedAt", Sort.Direction.DESC);

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
        try (MockedStatic<Utils> mockedUtils = Mockito.mockStatic(Utils.class)) {
            // Arrange
            String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
            String unsignedCredential = "{}";
            UUID credentialId = UUID.randomUUID();
            String signedCredential = "signedCredentialExample";
            String jsonResponse = "{\"data\":\"" + signedCredential + "\"}";

            when(accessTokenService.getCleanBearerToken(any())).thenReturn(Mono.just(mockTokenString));
            when(accessTokenService.getUserIdFromHeader(any())).thenReturn(Mono.just("1234567890"));
            when(appConfiguration.getRemoteSignatureDomain()).thenReturn("http://example.com");
            when(objectMapper.writeValueAsString(any())).thenReturn("SignatureRequest");
            when(objectMapper.readTree("{\"data\":\"signedCredentialExample\"}")).thenReturn(new ObjectMapper().readTree("{\"data\":\"signedCredentialExample\"}"));
            when(verifiableCredentialService.generateDeferredVcPayLoad(any()))
                    .thenReturn(Mono.just("vcPayload"));
            when(httpUtils.postRequest(any(), any(), any()))
                    .thenReturn(Mono.just(jsonResponse));
            when(credentialManagementService.updateCredential(any(), any(), any()))
                    .thenReturn(Mono.empty());

            // Act
            Mono<Void> result = controller.signVerifiableCredentials("Bearer " + mockTokenString, credentialId, unsignedCredential);

            // Assert
            StepVerifier.create(result)
                    .expectSubscription()
                    .verifyComplete();

            verify(verifiableCredentialService).generateDeferredVcPayLoad(anyString());
            verify(credentialManagementService).updateCredential(eq(signedCredential), eq(credentialId), anyString());
        }
    }
}