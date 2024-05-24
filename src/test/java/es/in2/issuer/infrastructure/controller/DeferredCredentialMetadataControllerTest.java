//package es.in2.issuer.infrastructure.controller;
//
//import es.in2.issuer.domain.model.*;
//import es.in2.issuer.domain.service.AccessTokenService;
//import es.in2.issuer.domain.service.CredentialManagementService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.sql.Timestamp;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class DeferredCredentialMetadataControllerTest {
//    @Mock
//    private CredentialManagementService credentialManagementService;
//
//    @Mock
//    private AccessTokenService accessTokenService;
//
//    @InjectMocks
//    private CredentialManagementController controller;
//
//    @Test
//    void testGetCredential(){
//        // Arrange
//        UUID credentialId = UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505");
//        Timestamp credentialModifiedAt = Timestamp.valueOf("2024-04-29 16:41:02.565");
//        Map<String, Object> credential = new HashMap<>();
//        credential.put("key1", "value1");
//
//        //Example Token with claim "sub" : "1234567890"
//        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
//        CredentialItem mockResponse = new CredentialItem(UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505"), credential, "jwt_vc_json", "VALID", credentialModifiedAt);
//
//        when(accessTokenService.getCleanBearerToken(any())).thenReturn(Mono.just(mockTokenString));
//        when(accessTokenService.getUserIdFromHeader(any())).thenReturn(Mono.just("1234567890"));
//        when(credentialManagementService.getCredential(credentialId, "1234567890"))
//                .thenReturn(Mono.just(mockResponse));
//
//        // Act
//        Mono<CredentialItem> result = controller.getCredential("Bearer "+mockTokenString, credentialId);
//
//        // Assert
//        result.subscribe(response -> assertEquals(mockResponse, response));
//
//        verify(credentialManagementService, times(1))
//                .getCredential(credentialId, "1234567890");
//    }
//
//    @Test
//    void testGetCredentials() {
//        // Arrange
//        UUID credentialId1 = UUID.fromString("b3787fd6-42a3-47ad-a2f7-26efdc742505");
//        UUID credentialId2 = UUID.fromString("a1987bc9-08f7-4b93-9177-67897b123456");
//        Timestamp timestamp = Timestamp.valueOf("2024-04-29 16:41:02.565");
//        Map<String, Object> credentialDetails = new HashMap<>();
//        credentialDetails.put("key1", "value1");
//
//        CredentialItem credentialItem1 = new CredentialItem(credentialId1, credentialDetails, "jwt_vc_json", "VALID", timestamp);
//        CredentialItem credentialItem2 = new CredentialItem(credentialId2, credentialDetails, "jwt_vc_json", "VALID", timestamp);
//
//        String mockTokenString = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcm5hbWUiLCJpYXQiOjE1MTYyMzkwMjJ9.3Ye-IUQRtSkYGVVZSjGqlVtnQNCsAwz_qPgkmgxkleg";
//
//        when(accessTokenService.getUserIdFromHeader(any())).thenReturn(Mono.just("1234567890"));
//        when(credentialManagementService.getCredentials("1234567890", 0, 10, "modifiedAt", Sort.Direction.DESC))
//                .thenReturn(Flux.just(credentialItem1, credentialItem2));
//
//        // Act
//        Flux<CredentialItem> result = controller.getCredentials("Bearer "+mockTokenString, 0, 10, "modifiedAt", Sort.Direction.DESC);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectNext(credentialItem1)
//                .expectNext(credentialItem2)
//                .verifyComplete();
//
//        verify(credentialManagementService, times(1))
//                .getCredentials("1234567890", 0, 10, "modifiedAt", Sort.Direction.DESC);
//    }
//
//    @Test
//    void testUpdateCredentials() {
//        WebTestClient webTestClient = WebTestClient.bindToController(controller).build();
//        String authorizationHeader = "Bearer test-token";
//        SignedCredentials signedCredentials = SignedCredentials.builder().credentials(Collections.singletonList(SignedCredentials.SignedCredential.builder().credential("test").build())).build(); // Assume this class exists
//        String userId = "user-id";
//
//        when(accessTokenService.getUserIdFromHeader(authorizationHeader)).thenReturn(Mono.just(userId));
//        when(credentialManagementService.updateCredentials(signedCredentials, userId)).thenReturn(Mono.empty());
//
//        webTestClient.post()
//                .uri("/api/v1/credentials")
//                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(signedCredentials)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//}