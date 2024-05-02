package es.in2.issuer.application.service.impl;


import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialIssuanceServiceImplTest {

    @Mock
    private RemoteSignatureService remoteSignatureService;
    @Mock
    private AuthenticSourcesRemoteService authenticSourcesRemoteService;
    @Mock
    private VerifiableCredentialService verifiableCredentialService;
    @Mock
    private AppConfiguration appConfiguration;
    @Mock
    private ProofValidationService proofValidationService;
    @Mock
    private CredentialManagementService credentialManagementService;
    @InjectMocks
    private VerifiableCredentialIssuanceServiceImpl service;

    String templateContent = """
            {
              "type": [
                "VerifiableCredential",
                "LEARCredential"
              ],
              "@context": [
                "https://www.w3.org/2018/credentials/v1",
                "https://issueridp.dev.in2.es/2022/credentials/learcredential/v1"
              ],
              "id": "urn:uuid:84f6fe0b-7cc8-460e-bb54-f805f0984202",
              "issuer": {
                "id": "did:elsi:VATES-Q0801175A"
              },
              "issuanceDate": "2024-03-08T18:27:46Z",
              "issued": "2024-03-08T18:27:46Z",
              "validFrom": "2024-03-08T18:27:46Z",
              "expirationDate": "2024-04-07T18:27:45Z",
              "credentialSubject": {}
            }""";

    @BeforeEach
    public void setUp() throws IOException {
        Resource mockResource = mock(Resource.class);
        lenient().when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8)));

        ReflectionTestUtils.setField(service, "learCredentialTemplate", mockResource);
    }

    @Test
    void testGenerateVerifiableCredentialResponse() throws UserDoesNotExistException{
        String did = "did:key:zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC";
        String jwtProof = "eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg";
        String userId = "user123";
        String token = "dummyToken";
        String unsignedCredential = "unsignedCredential";
        String transactionId = "1234";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .proof(
                        Proof.builder().proofType("jwt").jwt(jwtProof).build())
                .format(JWT_VC)
                .build();
        VerifiableCredentialResponse expectedResponse = VerifiableCredentialResponse.builder()
                .credential(unsignedCredential)
                .transactionId(transactionId)
                .cNonce("89XxmwL2RmGl2RZu-MTSyQ==")
                .cNonceExpiresIn(600)
                .build();

        when(proofValidationService.isProofValid(jwtProof)).thenReturn(Mono.just(true));
        when(authenticSourcesRemoteService.getUserFromLocalFile()).thenReturn(Mono.just("userData"));
        when(appConfiguration.getIssuerDid()).thenReturn("did:example:issuer");
        when(verifiableCredentialService.generateVc(
                        eq(templateContent),eq(did),eq("did:example:issuer"),eq("userData"),any(Instant.class))
                )
                .thenReturn(Mono.just(unsignedCredential));
        when(credentialManagementService.commitCredential(unsignedCredential, userId,credentialRequest.format())).thenReturn(Mono.just(transactionId));

        StepVerifier.create(service.generateVerifiableCredentialResponse(userId, credentialRequest, token))
                .assertNext(response -> assertEquals(expectedResponse, response))
                .verifyComplete();

    }

    @Test
    void generateVerifiableCredentialResponse_InvalidProof() {
        String userId = "user123";
        String token = "dummyToken";
        CredentialRequest credentialRequest = CredentialRequest.builder()
                .proof(
                        Proof.builder().proofType("jwt").jwt("invalidProof").build())
                .format(JWT_VC)
                .build();

        // Mock the proof validation to return false indicating the proof is invalid
        when(proofValidationService.isProofValid(credentialRequest.proof().jwt())).thenReturn(Mono.just(false));

        // Execute the method under test
        Mono<VerifiableCredentialResponse> response = service.generateVerifiableCredentialResponse(userId, credentialRequest, token);

        // Verify that an error is emitted and it is of type InvalidOrMissingProofException
        StepVerifier.create(response)
                .expectErrorMatches(throwable -> throwable instanceof InvalidOrMissingProofException && throwable.getMessage().contains("Invalid proof"))
                .verify();

        // Verify interactions
        verify(proofValidationService).isProofValid(credentialRequest.proof().jwt());
        // Ensure no other processes are initiated due to invalid proof
        verifyNoMoreInteractions(authenticSourcesRemoteService, verifiableCredentialService, credentialManagementService);
    }


    @Test
    void generateBatchVerifiableCredentialResponse_Success() throws UserDoesNotExistException {
        String did = "did:key:zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC";
        String jwtProof = "eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg";
        String unsignedCredential = "unsignedCredential";

        // Define your test input here
        String username = "testUser";
        String transactionId = "1234";
        CredentialRequest request = new CredentialRequest(JWT_VC, new CredentialDefinition(List.of("")),new Proof("type",jwtProof));
        BatchCredentialRequest batchCredentialRequest = new BatchCredentialRequest(List.of(request));
        String token = "testToken";
        VerifiableCredentialResponse expectedResponse = VerifiableCredentialResponse.builder()
                .credential(unsignedCredential)
                .transactionId(transactionId)
                .cNonce("89XxmwL2RmGl2RZu-MTSyQ==")
                .cNonceExpiresIn(600)
                .build();
        BatchCredentialResponse expectedResponses = BatchCredentialResponse.builder()
                        .credentialResponses(List.of(
                                BatchCredentialResponse.CredentialResponse
                                        .builder()
                                        .credential(expectedResponse.credential())
                                        .build()))
                .build();

        when(proofValidationService.isProofValid(jwtProof)).thenReturn(Mono.just(true));
        when(authenticSourcesRemoteService.getUserFromLocalFile()).thenReturn(Mono.just("userData"));
        when(appConfiguration.getIssuerDid()).thenReturn("did:example:issuer");
        when(verifiableCredentialService.generateVc(
                eq(templateContent),eq(did),eq("did:example:issuer"),eq("userData"),any(Instant.class))
        )
                .thenReturn(Mono.just(unsignedCredential));
        when(credentialManagementService.commitCredential(unsignedCredential, username,batchCredentialRequest.credentialRequests().get(0).format())).thenReturn(Mono.just(transactionId));

        // Test the method
        Mono<BatchCredentialResponse> result = service.generateVerifiableCredentialBatchResponse(username, batchCredentialRequest, token);

        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(expectedResponses, response))
                .verifyComplete();

    }

    @Test
    void generateVerifiableCredentialDeferredResponse_NotSigned() {
        String userId = "user123";
        String token = "dummyToken";
        String transactionId = "1234";
        CredentialDeferred deferredCredential = CredentialDeferred.builder().credentialSigned(null).transactionId(transactionId).build();

        when(credentialManagementService.getDeferredCredentialByTransactionId(transactionId))
                .thenReturn(Mono.just(deferredCredential));
        when(credentialManagementService.updateTransactionId(transactionId))
                .thenReturn(Mono.just("4321"));

        VerifiableCredentialResponse expectedResponse = VerifiableCredentialResponse.builder()
                .transactionId("4321")
                .build();

        StepVerifier.create(service.generateVerifiableCredentialDeferredResponse(userId, new DeferredCredentialRequest(transactionId), token))
                .assertNext(response -> assertEquals(expectedResponse, response))
                .verifyComplete();
    }

    @Test
    void generateVerifiableCredentialDeferredResponse_Signed() {
        String userId = "user123";
        String token = "dummyToken";
        String transactionId = "1234";
        String credential = "signed credential";
        CredentialDeferred deferredCredential = CredentialDeferred.builder().credentialSigned(credential).transactionId(transactionId).build();


        when(credentialManagementService.getDeferredCredentialByTransactionId(transactionId))
                .thenReturn(Mono.just(deferredCredential));
        when(credentialManagementService.deleteCredentialDeferred(transactionId))
                .thenReturn(Mono.empty());

        VerifiableCredentialResponse expectedResponse = VerifiableCredentialResponse.builder()
                .credential(credential)
                .build();

        StepVerifier.create(service.generateVerifiableCredentialDeferredResponse(userId, new DeferredCredentialRequest(transactionId), token))
                .assertNext(response -> assertEquals(expectedResponse, response))
                .verifyComplete();
    }

    @Test
    void signDeferredCredential_Success() {
        String unsignedCredential = "unsignedCredential";
        String userId = "user123";
        UUID credentialId = UUID.randomUUID();
        String token = "dummyToken";
        String signedCredential = "signedCredentialData";

        when(verifiableCredentialService.generateDeferredVcPayLoad(unsignedCredential))
                .thenReturn(Mono.just("vcPayload"));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));
        when(credentialManagementService.updateCredential(signedCredential, credentialId, userId))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.signDeferredCredential(unsignedCredential, userId, credentialId, token))
                .verifyComplete();
    }

    @Test
    void signCredentialOnRequestedFormat_JWT_Success() {
        String unsignedCredential = "unsignedCredential";
        String userId = "user123";
        UUID credentialId = UUID.randomUUID();
        String token = "dummyToken";
        String signedCredential = "signedJWTData";

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));

        StepVerifier.create(service.signCredentialOnRequestedFormat(unsignedCredential, JWT_VC, userId, credentialId, token))
                .assertNext(signedData -> assertEquals(signedCredential, signedData))
                .verifyComplete();
    }

    @Test
    void signCredentialOnRequestedFormat_UnsupportedFormat() {
        String unsignedCredential = "unsignedCredential";
        String userId = "user123";
        UUID credentialId = UUID.randomUUID();
        String token = "dummyToken";
        String unsupportedFormat = "unsupportedFormat";

        StepVerifier.create(service.signCredentialOnRequestedFormat(unsignedCredential, unsupportedFormat, userId, credentialId, token))
                .expectError(IllegalArgumentException.class)
                .verify();
    }


}