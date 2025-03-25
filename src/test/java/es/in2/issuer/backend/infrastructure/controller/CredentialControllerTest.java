package es.in2.issuer.backend.infrastructure.controller;

import es.in2.issuer.backend.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.backend.domain.model.dto.*;
import es.in2.issuer.backend.infrastructure.controller.CredentialController;
import es.in2.issuer.backend.domain.service.AccessTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialControllerTest {

    @Mock
    private VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private CredentialController credentialController;

    @Test
    void createVerifiableCredential() {
        //Arrange
        String authorizationHeader = "Bearer testToken";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .format("sampleFormat")
                .credentialDefinition(CredentialDefinition.builder().type(List.of("type")).build())
                .build();
        VerifiableCredentialResponse verifiableCredentialResponse = VerifiableCredentialResponse.builder()
                .credential("sampleCredential")
                .transactionId("sampleTransactionId")
                .cNonce("sampleCNonce")
                .cNonceExpiresIn(35)
                .build();
        ResponseEntity<VerifiableCredentialResponse> expectedResponse =  new ResponseEntity<>(verifiableCredentialResponse, HttpStatus.ACCEPTED);
        when(accessTokenService.getCleanBearerToken(authorizationHeader)).thenReturn(Mono.just("testToken"));
        when(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(anyString(), eq(credentialRequest), anyString())).thenReturn(Mono.just(verifiableCredentialResponse));

        Mono<ResponseEntity<VerifiableCredentialResponse>> result = credentialController.createVerifiableCredential(authorizationHeader, credentialRequest);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(expectedResponse, response))
                .verifyComplete();
    }

    @Test
    void getCredential() {
        String authorizationHeader = "Bearer testToken";
        String newTransactionId = "newTransactionId";
        DeferredCredentialRequest deferredCredentialRequest = DeferredCredentialRequest.builder()
                .transactionId(newTransactionId)
                .build();
        VerifiableCredentialResponse verifiableCredentialResponse = VerifiableCredentialResponse.builder()
                .credential("sampleCredential")
                .transactionId("sampleTransactionId")
                .cNonce("sampleCNonce")
                .cNonceExpiresIn(35)
                .build();
        when(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialDeferredResponse(anyString(), eq(deferredCredentialRequest))).thenReturn(Mono.just(verifiableCredentialResponse));

        Mono<VerifiableCredentialResponse> result = credentialController.getCredential(authorizationHeader, deferredCredentialRequest);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(verifiableCredentialResponse, response))
                .verifyComplete();
    }

    @Test
    void createVerifiableCredentials() {
        String authorizationHeader = "Bearer testToken";
        BatchCredentialResponse.CredentialResponse newCredentialResponse = new BatchCredentialResponse.CredentialResponse("newSampleCredential");
        List<BatchCredentialResponse.CredentialResponse> newCredentialResponses = List.of(newCredentialResponse);
        String expectedFormat = "sampleFormat";
        Proof expectedProof = new Proof("jwt_vc_json", "sampleJwt");
        CredentialDefinition expectedCredentialDefinition = new CredentialDefinition(List.of("type"));
        BatchCredentialRequest batchCredentialRequest = new BatchCredentialRequest(List.of(new CredentialRequest(expectedFormat, expectedCredentialDefinition, expectedProof)));
        BatchCredentialResponse batchCredentialResponse = BatchCredentialResponse.builder()
                .credentialResponses(newCredentialResponses)
                .build();

        when(accessTokenService.getCleanBearerToken(authorizationHeader)).thenReturn(Mono.just("testToken"));
        when(accessTokenService.getUserId(authorizationHeader)).thenReturn(Mono.just("testUserId"));
        when(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialBatchResponse(anyString(), eq(batchCredentialRequest), anyString())).thenReturn(Mono.just(batchCredentialResponse));

        Mono<BatchCredentialResponse> result = credentialController.createVerifiableCredentials(authorizationHeader, batchCredentialRequest);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(batchCredentialResponse, response))
                .verifyComplete();
    }
}