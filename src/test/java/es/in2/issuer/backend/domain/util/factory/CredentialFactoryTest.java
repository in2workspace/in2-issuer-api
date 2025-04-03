package es.in2.issuer.backend.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import es.in2.issuer.backend.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.backend.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.backend.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.backend.domain.service.CredentialProcedureService;
import es.in2.issuer.backend.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.backend.domain.util.factory.CredentialFactory;
import es.in2.issuer.backend.domain.util.factory.LEARCredentialEmployeeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static es.in2.issuer.backend.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialFactoryTest {

    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @InjectMocks
    private CredentialFactory credentialFactory;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Test
    void testMapCredentialIntoACredentialProcedureRequest_Success() {
        //Arrange
        String processId = "processId";
        String credentialType = "LEARCredentialEmployee";
        PreSubmittedCredentialRequest preSubmittedCredentialRequest = mock(PreSubmittedCredentialRequest.class);
        when(preSubmittedCredentialRequest.operationMode()).thenReturn("S");
        JsonNode jsonNode = mock(JsonNode.class);
        when(preSubmittedCredentialRequest.payload()).thenReturn(jsonNode);
        CredentialProcedureCreationRequest credentialProcedureCreationRequest = mock(CredentialProcedureCreationRequest.class);

        when(learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(jsonNode, preSubmittedCredentialRequest.operationMode()))
                .thenReturn(Mono.just(credentialProcedureCreationRequest));

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, credentialType, preSubmittedCredentialRequest, "token"))
                .expectNext(credentialProcedureCreationRequest)
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapAndBuildLEARCredentialEmployee(jsonNode, preSubmittedCredentialRequest.operationMode());
    }

    @Test
    void testMapCredentialIntoACredentialProcedureRequest_Failure() {
        //Arrange
        String processId = "processId";
        String credentialType = "UNSUPPORTED_CREDENTIAL";
        PreSubmittedCredentialRequest preSubmittedCredentialRequest = mock(PreSubmittedCredentialRequest.class);

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, credentialType, preSubmittedCredentialRequest, "token"))
                .expectError(CredentialTypeUnsupportedException.class)
                .verify();

        verify(learCredentialEmployeeFactory, never()).mapAndBuildLEARCredentialEmployee(any(), any());
    }

    @Test
    void testMapCredentialAndBindMandateeId_Success() {
        //Arrange
        String processId = "processId";
        String credentialType = "LEARCredentialEmployee";
        String credential = "credential";
        String mandateeId = "mandateeId";
        String result = "result";

        when(learCredentialEmployeeFactory.mapCredentialAndBindMandateeIdInToTheCredential(credential, mandateeId))
                .thenReturn(Mono.just(result));

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, credential, mandateeId))
                .expectNext(result)
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapCredentialAndBindMandateeIdInToTheCredential(credential, mandateeId);
    }

    @Test
    void testMapCredentialAndBindMandateeId_Failure() {
        //Arrange
        String processId = "processId";
        String credentialType = "UNSUPPORTED_CREDENTIAL";
        String credential = "credential";
        String mandateeId = "mandateeId";

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, credential, mandateeId))
                .expectError(CredentialTypeUnsupportedException.class)
                .verify();

        verify(learCredentialEmployeeFactory, never()).mapCredentialAndBindMandateeIdInToTheCredential(anyString(), anyString());
    }

    @Test
    void mapCredentialBindIssuerAndUpdateDB_Success() {
        String processId = "processId";
        String procedureId = "procedureId";
        String decodedCredential = "decodedCredential";
        String boundCredential = "boundCredential";
        String format = "format";
        String authServerNonce = "nonce";

        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId))
                .thenReturn(Mono.just(boundCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, boundCredential, format))
                .thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.updateDeferredCredentialByAuthServerNonce(authServerNonce, format))
                .thenReturn(Mono.empty());

        StepVerifier.create(credentialFactory.mapCredentialBindIssuerAndUpdateDB(processId, procedureId, decodedCredential, LEAR_CREDENTIAL_EMPLOYEE, format, authServerNonce))
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId);
        verify(credentialProcedureService).updateDecodedCredentialByProcedureId(procedureId, boundCredential, format);
        verify(deferredCredentialMetadataService).updateDeferredCredentialByAuthServerNonce(authServerNonce, format);
    }

    @Test
    void mapCredentialBindIssuerAndUpdateDB_UnsupportedCredentialType_Error() {
        String processId = "processId";
        String procedureId = "procedureId";
        String decodedCredential = "decodedCredential";
        String credentialType = "unsupportedType";
        String format = "format";
        String authServerNonce = "nonce";

        StepVerifier.create(credentialFactory.mapCredentialBindIssuerAndUpdateDB(processId, procedureId, decodedCredential, credentialType, format, authServerNonce))
                .expectError(CredentialTypeUnsupportedException.class)
                .verify();

        verify(learCredentialEmployeeFactory, never()).mapCredentialAndBindIssuerInToTheCredential(any(), any());
        verify(credentialProcedureService, never()).updateDecodedCredentialByProcedureId(any(), any(), any());
        verify(deferredCredentialMetadataService, never()).updateDeferredCredentialMetadataByAuthServerNonce(any(), any());
    }

    @Test
    void mapCredentialBindIssuerAndUpdateDB_BindIssuer_Error() {
        String processId = "processId";
        String procedureId = "procedureId";
        String decodedCredential = "decodedCredential";
        String format = "format";
        String authServerNonce = "nonce";

        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId))
                .thenReturn(Mono.error(new RuntimeException("Binding error")));

        StepVerifier.create(credentialFactory.mapCredentialBindIssuerAndUpdateDB(processId, procedureId, decodedCredential, LEAR_CREDENTIAL_EMPLOYEE, format, authServerNonce))
                .expectError(RuntimeException.class)
                .verify();

        verify(learCredentialEmployeeFactory).mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId);
        verify(credentialProcedureService, never()).updateDecodedCredentialByProcedureId(any(), any(), any());
        verify(deferredCredentialMetadataService, never()).updateDeferredCredentialMetadataByAuthServerNonce(any(), any());
    }

    @Test
    void mapCredentialBindIssuerAndUpdateDB_UpdateDB_Error() {
        String processId = "processId";
        String procedureId = "procedureId";
        String decodedCredential = "decodedCredential";
        String boundCredential = "boundCredential";
        String format = "format";
        String authServerNonce = "nonce";

        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId))
                .thenReturn(Mono.just(boundCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, boundCredential, format))
                .thenReturn(Mono.error(new RuntimeException("DB Update error")));

        StepVerifier.create(credentialFactory.mapCredentialBindIssuerAndUpdateDB(processId, procedureId, decodedCredential, LEAR_CREDENTIAL_EMPLOYEE, format, authServerNonce))
                .expectError(RuntimeException.class)
                .verify();

        verify(learCredentialEmployeeFactory).mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId);
        verify(credentialProcedureService).updateDecodedCredentialByProcedureId(procedureId, boundCredential, format);
        verify(deferredCredentialMetadataService).updateDeferredCredentialByAuthServerNonce(authServerNonce, format);
    }




}