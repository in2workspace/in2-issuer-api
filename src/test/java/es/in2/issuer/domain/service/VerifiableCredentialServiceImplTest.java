package es.in2.issuer.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.service.impl.VerifiableCredentialServiceImpl;
import es.in2.issuer.domain.util.Constants;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static es.in2.issuer.domain.util.Constants.BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialServiceImplTest {

    private final String processId = "process-id-123";
    private final String preAuthCode = "pre-auth-code-456";
    private final String transactionId = "transaction-id-789";
    private final String deferredResponseId = "deferred-response-id-456";
    private final String procedureId = "procedure-id-321";
    private final String vcValue = "vc-value-123";
    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;
    @Mock
    private CredentialFactory credentialFactory;
    @Mock
    private CredentialProcedureService credentialProcedureService;
    @Mock
    private CredentialSignerWorkflow credentialSignerWorkflow;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private VerifiableCredentialServiceImpl verifiableCredentialServiceImpl;

    @Test
    void bindAccessTokenByPreAuthorizedCode_Success() {
        // Arrange: Mock the service to return a Mono.empty()
        String expectedJti = "expected-jti-value";
        when(deferredCredentialMetadataService.updateAuthServerNonceByAuthServerNonce(expectedJti, preAuthCode))
                .thenReturn(Mono.empty());

        // Act: Call the method
        String validAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJleHBlY3RlZC1qdGktdmFsdWUifQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Mono<Void> result = verifiableCredentialServiceImpl.bindAccessTokenByPreAuthorizedCode(processId, validAccessToken, preAuthCode);

        // Assert: Verify the interactions and result
        StepVerifier.create(result)
                .verifyComplete();

        verify(deferredCredentialMetadataService, times(1))
                .updateAuthServerNonceByAuthServerNonce(expectedJti, preAuthCode);
    }

    @Test
    void bindAccessTokenByPreAuthorizedCode_InvalidToken_ThrowsException() {
        // Arrange: Use an invalid JWT token
        String invalidAccessToken = "invalid-token";

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            verifiableCredentialServiceImpl.bindAccessTokenByPreAuthorizedCode(processId, invalidAccessToken, preAuthCode).block());
        assertNull(exception.getMessage());

        // Verify that no interaction with deferredCredentialMetadataService happens
        verify(deferredCredentialMetadataService, times(0))
                .updateAuthServerNonceByAuthServerNonce(anyString(), anyString());
    }

    @Test
    void generateVc_Success() throws Exception {
        // Arrange: Create a sample JsonNode for LEARCredentialRequest
        String token = "token";
        JsonNode credentialJsonNode = objectMapper.readTree("{\"credentialId\":\"cred-id-123\", \"organizationIdentifier\":\"org-id-123\", \"credentialDecoded\":\"decoded-credential\"}");
        IssuanceRequest issuanceRequest = IssuanceRequest.builder()
                .payload(credentialJsonNode)
                .build();

        // Mock the behavior of credentialFactory
        CredentialProcedureCreationRequest mockCreationRequest = CredentialProcedureCreationRequest.builder()
                .credentialId("cred-id-123")
                .organizationIdentifier("org-id-123")
                .credentialDecoded("decoded-credential")
                .build();
        String vcType = "vc-type-789";
        when(credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, credentialJsonNode,token))
                .thenReturn(Mono.just(mockCreationRequest));

        // Mock the behavior of credentialProcedureService
        String createdProcedureId = "created-procedure-id-456";
        when(credentialProcedureService.createCredentialProcedure(mockCreationRequest))
                .thenReturn(Mono.just(createdProcedureId));

        // Mock the behavior of deferredCredentialMetadataService
        String metadataId = "metadata-id-789";
        when(deferredCredentialMetadataService.createDeferredCredentialMetadata(createdProcedureId, null, null))
                .thenReturn(Mono.just(metadataId));

        // Act: Call the generateVc method
        Mono<String> result = verifiableCredentialServiceImpl.generateVc(processId, vcType, issuanceRequest, token);

        // Assert: Verify the result
        StepVerifier.create(result)
                .expectNext(metadataId)
                .verifyComplete();

        // Verify that all the interactions occurred as expected
        verify(credentialFactory, times(1))
                .mapCredentialIntoACredentialProcedureRequest(processId, vcType, credentialJsonNode, token);

        verify(credentialProcedureService, times(1))
                .createCredentialProcedure(mockCreationRequest);

        verify(deferredCredentialMetadataService, times(1))
                .createDeferredCredentialMetadata(createdProcedureId, null, null);
    }

    @Test
    void generateDeferredCredentialResponse_WithVcPresent() {
        // Arrange: Create the request and mock response
        DeferredCredentialRequest deferredCredentialRequest = DeferredCredentialRequest.builder()
                .transactionId(transactionId)
                .build();

        DeferredCredentialMetadataDeferredResponse mockResponseWithVc = DeferredCredentialMetadataDeferredResponse.builder()
                .id(deferredResponseId)
                .procedureId(procedureId)
                .transactionId(transactionId)
                .vc(vcValue)
                .build();

        // Mock the service methods
        when(deferredCredentialMetadataService.getVcByTransactionId(transactionId))
                .thenReturn(Mono.just(mockResponseWithVc));
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.deleteDeferredCredentialMetadataById(deferredResponseId))
                .thenReturn(Mono.empty());

        // Act: Call the method
        Mono<VerifiableCredentialResponse> result = verifiableCredentialServiceImpl.generateDeferredCredentialResponse(processId, deferredCredentialRequest);

        // Assert: Verify the result
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.credential().equals(vcValue) && response.transactionId() == null)
                .verifyComplete();

        // Verify the interactions
        verify(deferredCredentialMetadataService, times(1))
                .getVcByTransactionId(transactionId);
        verify(credentialProcedureService, times(1))
                .updateCredentialProcedureCredentialStatusToValidByProcedureId((procedureId));
        verify(deferredCredentialMetadataService, times(1))
                .deleteDeferredCredentialMetadataById(deferredResponseId);
    }

    @Test
    void generateDeferredCredentialResponse_WithVcAbsent() {
        // Arrange: Create the request and mock response
        DeferredCredentialRequest deferredCredentialRequest = DeferredCredentialRequest.builder()
                .transactionId(transactionId)
                .build();

        DeferredCredentialMetadataDeferredResponse mockResponseWithoutVc = DeferredCredentialMetadataDeferredResponse.builder()
                .id(deferredResponseId)
                .procedureId(procedureId)
                .transactionId(transactionId)
                .vc(null) // No VC present
                .build();

        // Mock the service methods
        when(deferredCredentialMetadataService.getVcByTransactionId(transactionId))
                .thenReturn(Mono.just(mockResponseWithoutVc));

        // Act: Call the method
        Mono<VerifiableCredentialResponse> result = verifiableCredentialServiceImpl.generateDeferredCredentialResponse(processId, deferredCredentialRequest);

        // Assert: Verify the result
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.transactionId().equals(transactionId) && response.credential() == null)
                .verifyComplete();

        // Verify the interactions
        verify(deferredCredentialMetadataService, times(1))
                .getVcByTransactionId(transactionId);
        verify(credentialProcedureService, times(0))
                .updateCredentialProcedureCredentialStatusToValidByProcedureId(anyString());
        verify(deferredCredentialMetadataService, times(0))
                .deleteDeferredCredentialMetadataById(anyString());
    }

    @Test
    void buildCredentialResponse_Success() throws Exception {
        // Mock the behavior of ObjectMapper to parse the JSON string into JsonNode
        when(objectMapper.readTree(anyString())).thenAnswer(invocation -> {
            String json = invocation.getArgument(0, String.class);
            return new ObjectMapper().readTree(json); // Use a new ObjectMapper to parse the string
        });

        // Mock the behavior of ObjectMapper to convert JsonNode to LEARCredentialEmployee
        when(objectMapper.treeToValue(any(JsonNode.class), eq(LEARCredentialEmployee.class)))
                .thenAnswer(invocation -> {
                    JsonNode node = invocation.getArgument(0, JsonNode.class);
                    return new ObjectMapper().treeToValue(node, LEARCredentialEmployee.class); // Use a new ObjectMapper to do the conversion
                });

        // Mock the behavior of ObjectMapper to convert LEARCredentialEmployee to JSON string
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class)))
                .thenAnswer(invocation -> {
                    LEARCredentialEmployee credential = invocation.getArgument(0, LEARCredentialEmployee.class);
                    return new ObjectMapper().writeValueAsString(credential); // Use a new ObjectMapper to do the conversion
                });

        // Arrange: Mock the service methods
        String authServerNonce = "auth-server-nonce-789";
        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce))
                .thenReturn(Mono.just(procedureId));

        String credentialType = "LEARCredentialEmployee";
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId))
                .thenReturn(Mono.just(credentialType));

        String decodedCredential = "{\"vc\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"id\":\"example-id\",\"type\":[\"VerifiableCredential\",\"LEARCredentialEmployee\"],\"credentialSubject\":{\"mandate\":{\"id\":\"mandate-id\",\"life_span\":{\"end_date_time\":\"2024-12-31T23:59:59Z\",\"start_date_time\":\"2023-01-01T00:00:00Z\"},\"mandatee\":{\"id\":\"mandatee-id\",\"email\":\"mandatee@example.com\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"mobile_phone\":\"+123456789\"},\"mandator\":{\"commonName\":\"Company ABC\",\"country\":\"Country XYZ\",\"emailAddress\":\"mandator@example.com\",\"organization\":\"Org ABC\",\"organizationIdentifier\":\"org-123\",\"serialNumber\":\"1234567890\"},\"power\":[{\"id\":\"power-id\",\"tmf_action\":\"action\",\"tmf_domain\":\"domain\",\"tmf_function\":\"function\",\"tmf_type\":\"type\"}]}}},\"expirationDate\":\"2024-12-31T23:59:59Z\",\"issuanceDate\":\"2023-01-01T00:00:00Z\",\"issuer\":\"did:example:issuer\",\"validFrom\":\"2023-01-01T00:00:00Z\"}}";
        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(decodedCredential));

        String subjectDid = "did:example:123456789";
        String bindCredential = "{\"vc\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"id\":\"example-id\",\"type\":[\"VerifiableCredential\",\"LEARCredentialEmployee\"],\"credentialSubject\":{\"mandate\":{\"id\":\"mandate-id\",\"life_span\":{\"end_date_time\":\"2024-12-31T23:59:59Z\",\"start_date_time\":\"2023-01-01T00:00:00Z\"},\"mandatee\":{\"id\":\"mandatee-id\",\"email\":\"mandatee@example.com\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"mobile_phone\":\"+123456789\"},\"mandator\":{\"commonName\":\"Company ABC\",\"country\":\"Country XYZ\",\"emailAddress\":\"mandator@example.com\",\"organization\":\"Org ABC\",\"organizationIdentifier\":\"org-123\",\"serialNumber\":\"1234567890\"},\"power\":[{\"id\":\"power-id\",\"tmf_action\":\"action\",\"tmf_domain\":\"domain\",\"tmf_function\":\"function\",\"tmf_type\":\"type\"}]}}},\"expirationDate\":\"2024-12-31T23:59:59Z\",\"issuanceDate\":\"2023-01-01T00:00:00Z\",\"issuer\":\"did:example:issuer\",\"validFrom\":\"2023-01-01T00:00:00Z\"}}";
        when(credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, decodedCredential, subjectDid))
                .thenReturn(Mono.just(bindCredential));

        String format = "json";
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format))
                .thenReturn(Mono.empty());

        when(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format))
                .thenReturn(Mono.just(transactionId));

        // Act: Call the method
        Mono<VerifiableCredentialResponse> result = verifiableCredentialServiceImpl.buildCredentialResponse(processId, subjectDid, authServerNonce, format, "token", "A");

        // Convert the bindCredential JSON string to LEARCredentialEmployee
        JsonNode vcNode = objectMapper.readTree(bindCredential).get("vc");
        LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);

        // Log the intermediate LEARCredentialEmployee object for debugging
        System.out.println("Parsed LEARCredentialEmployee: " + learCredential);

        // Convert the LEARCredentialEmployee to JSON string for expected value
        String expectedCredentialJson = objectMapper.writeValueAsString(learCredential);

        // Log the expected credential JSON for debugging
        System.out.println("Expected Credential JSON: " + expectedCredentialJson);

        // Assert: Verify the result
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    // Log the response for debugging
                    System.out.println("Response: " + response);
                    return response.credential().equals(expectedCredentialJson) &&
                            response.transactionId().equals(transactionId);
                })
                .verifyComplete();

        // Verify the interactions
        verify(deferredCredentialMetadataService, times(1))
                .getProcedureIdByAuthServerNonce(authServerNonce);

        verify(credentialProcedureService, times(1))
                .getCredentialTypeByProcedureId(procedureId);

        verify(credentialProcedureService, times(1))
                .getDecodedCredentialByProcedureId(procedureId);

        verify(credentialFactory, times(1))
                .mapCredentialAndBindMandateeId(processId, credentialType, decodedCredential, subjectDid);

        verify(credentialProcedureService, times(1))
                .updateDecodedCredentialByProcedureId(procedureId, bindCredential, format);

        verify(deferredCredentialMetadataService, times(1))
                .updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format);
    }

    @Test
    void buildCredentialResponseSync_Success() throws Exception {
        // Mock the behavior of ObjectMapper to parse the JSON string into JsonNode
        when(objectMapper.readTree(anyString())).thenAnswer(invocation -> {
            String json = invocation.getArgument(0, String.class);
            return new ObjectMapper().readTree(json); // Use a new ObjectMapper to parse the string
        });

        // Mock the behavior of ObjectMapper to convert JsonNode to LEARCredentialEmployee
        when(objectMapper.treeToValue(any(JsonNode.class), eq(LEARCredentialEmployee.class)))
                .thenAnswer(invocation -> {
                    JsonNode node = invocation.getArgument(0, JsonNode.class);
                    return new ObjectMapper().treeToValue(node, LEARCredentialEmployee.class); // Use a new ObjectMapper to do the conversion
                });

        // Mock the behavior of ObjectMapper to convert LEARCredentialEmployee to JSON string
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class)))
                .thenAnswer(invocation -> {
                    LEARCredentialEmployee credential = invocation.getArgument(0, LEARCredentialEmployee.class);
                    return new ObjectMapper().writeValueAsString(credential); // Use a new ObjectMapper to do the conversion
                });

        // Arrange: Mock the service methods
        String authServerNonce = "auth-server-nonce-789";
        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce))
                .thenReturn(Mono.just(procedureId));

        String credentialType = "LEARCredentialEmployee";
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId))
                .thenReturn(Mono.just(credentialType));

        String decodedCredential = "{\"vc\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"id\":\"example-id\",\"type\":[\"VerifiableCredential\",\"LEARCredentialEmployee\"],\"credentialSubject\":{\"mandate\":{\"id\":\"mandate-id\",\"life_span\":{\"end_date_time\":\"2024-12-31T23:59:59Z\",\"start_date_time\":\"2023-01-01T00:00:00Z\"},\"mandatee\":{\"id\":\"mandatee-id\",\"email\":\"mandatee@example.com\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"mobile_phone\":\"+123456789\"},\"mandator\":{\"commonName\":\"Company ABC\",\"country\":\"Country XYZ\",\"emailAddress\":\"mandator@example.com\",\"organization\":\"Org ABC\",\"organizationIdentifier\":\"org-123\",\"serialNumber\":\"1234567890\"},\"power\":[{\"id\":\"power-id\",\"tmf_action\":\"action\",\"tmf_domain\":\"domain\",\"tmf_function\":\"function\",\"tmf_type\":\"type\"}]}}},\"expirationDate\":\"2024-12-31T23:59:59Z\",\"issuanceDate\":\"2023-01-01T00:00:00Z\",\"issuer\":\"did:example:issuer\",\"validFrom\":\"2023-01-01T00:00:00Z\"}}";
        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(decodedCredential));

        String subjectDid = "did:example:123456789";
        String bindCredential = "{\"vc\":{\"@context\":[\"https://www.w3.org/2018/credentials/v1\"],\"id\":\"example-id\",\"type\":[\"VerifiableCredential\",\"LEARCredentialEmployee\"],\"credentialSubject\":{\"mandate\":{\"id\":\"mandate-id\",\"life_span\":{\"end_date_time\":\"2024-12-31T23:59:59Z\",\"start_date_time\":\"2023-01-01T00:00:00Z\"},\"mandatee\":{\"id\":\"mandatee-id\",\"email\":\"mandatee@example.com\",\"first_name\":\"John\",\"last_name\":\"Doe\",\"mobile_phone\":\"+123456789\"},\"mandator\":{\"commonName\":\"Company ABC\",\"country\":\"Country XYZ\",\"emailAddress\":\"mandator@example.com\",\"organization\":\"Org ABC\",\"organizationIdentifier\":\"org-123\",\"serialNumber\":\"1234567890\"},\"power\":[{\"id\":\"power-id\",\"tmf_action\":\"action\",\"tmf_domain\":\"domain\",\"tmf_function\":\"function\",\"tmf_type\":\"type\"}]}}},\"expirationDate\":\"2024-12-31T23:59:59Z\",\"issuanceDate\":\"2023-01-01T00:00:00Z\",\"issuer\":\"did:example:issuer\",\"validFrom\":\"2023-01-01T00:00:00Z\"}}";
        when(credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, decodedCredential, subjectDid))
                .thenReturn(Mono.just(bindCredential));

        String format = "json";
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format))
                .thenReturn(Mono.empty());

        when(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format))
                .thenReturn(Mono.just(transactionId));

        when(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)).thenReturn(Mono.just(procedureId));
        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + "token", procedureId, Constants.JWT_VC)).thenReturn(Mono.just("signedCredential"));

        // Act: Call the method
        Mono<VerifiableCredentialResponse> result = verifiableCredentialServiceImpl.buildCredentialResponse(processId, subjectDid, authServerNonce, format, "token", "S");

        // Convert the bindCredential JSON string to LEARCredentialEmployee
        JsonNode vcNode = objectMapper.readTree(bindCredential).get("vc");
        LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);

        // Log the intermediate LEARCredentialEmployee object for debugging
        System.out.println("Parsed LEARCredentialEmployee: " + learCredential);

        // Convert the LEARCredentialEmployee to JSON string for expected value
        String expectedCredentialJson = objectMapper.writeValueAsString(learCredential);

        // Log the expected credential JSON for debugging
        System.out.println("Expected Credential JSON: " + expectedCredentialJson);

        // Assert: Verify the result
        StepVerifier.create(result)
                .expectNextMatches(response -> {
                    // Log the response for debugging
                    System.out.println("Response: " + response);
                    return response.credential().equals("signedCredential");
                })
                .verifyComplete();

        // Verify the interactions
        verify(credentialProcedureService, times(1))
                .getCredentialTypeByProcedureId(procedureId);

        verify(credentialProcedureService, times(1))
                .getDecodedCredentialByProcedureId(procedureId);

        verify(credentialFactory, times(1))
                .mapCredentialAndBindMandateeId(processId, credentialType, decodedCredential, subjectDid);

        verify(credentialProcedureService, times(1))
                .updateDecodedCredentialByProcedureId(procedureId, bindCredential, format);

        verify(deferredCredentialMetadataService, times(1))
                .updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format);
    }
}