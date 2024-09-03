package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.AccessTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void createWithdrawnLEARCredential() {
        String type = "testType";
        CredentialData credentialData = CredentialData.builder()
                .payload(null)
                .build();
        when(verifiableCredentialIssuanceWorkflow.completeWithdrawCredentialProcess(anyString(), eq(type), eq(credentialData), anyString())).thenReturn(Mono.empty());

        Mono<Void> result = credentialController.createWithdrawnLEARCredential(type, "authorizationHeader", credentialData);

        StepVerifier.create(result)
                .verifyComplete();
    }

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
        when(accessTokenService.getCleanBearerToken(authorizationHeader)).thenReturn(Mono.just("testToken"));
        when(verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(anyString(), eq(credentialRequest), anyString())).thenReturn(Mono.just(verifiableCredentialResponse));

        Mono<VerifiableCredentialResponse> result = credentialController.createVerifiableCredential(authorizationHeader, credentialRequest);

        StepVerifier.create(result)
                .assertNext(response -> assertEquals(verifiableCredentialResponse, response))
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
        BatchCredentialRequest batchCredentialRequest = new BatchCredentialRequest(List.of(new CredentialRequest(expectedFormat, expectedCredentialDefinition, "LEARCredentialEmployee", expectedProof)));
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