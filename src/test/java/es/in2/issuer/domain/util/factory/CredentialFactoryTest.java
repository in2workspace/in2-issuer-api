package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialFactoryTest {

    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @InjectMocks
    private CredentialFactory credentialFactory;

    @Test
    void testMapCredentialIntoACredentialProcedureRequest_Success() {
        //Arrange
        String processId = "processId";
        String credentialType = "LEARCredentialEmployee";
        JsonNode jsonNode = mock(JsonNode.class);
        CredentialProcedureCreationRequest credentialProcedureCreationRequest = mock(CredentialProcedureCreationRequest.class);

        when(learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(jsonNode))
                .thenReturn(Mono.just(credentialProcedureCreationRequest));

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, credentialType, jsonNode))
                .expectNext(credentialProcedureCreationRequest)
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapAndBuildLEARCredentialEmployee(jsonNode);
    }

    @Test
    void testMapCredentialIntoACredentialProcedureRequest_Failure() {
        //Arrange
        String processId = "processId";
        String credentialType = "UNSUPPORTED_CREDENTIAL";
        JsonNode jsonNode = mock(JsonNode.class);

        //Act & Assert
        StepVerifier.create(credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, credentialType, jsonNode))
                .expectError(CredentialTypeUnsupportedException.class)
                .verify();

        verify(learCredentialEmployeeFactory, never()).mapAndBuildLEARCredentialEmployee(any());
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
        StepVerifier.create(credentialFactory.mapCredentialBasedOnType(processId, credentialType, credential, mandateeId))
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
        StepVerifier.create(credentialFactory.mapCredentialBasedOnType(processId, credentialType, credential, mandateeId))
                .expectError(CredentialTypeUnsupportedException.class)
                .verify();

        verify(learCredentialEmployeeFactory, never()).mapCredentialAndBindMandateeIdInToTheCredential(anyString(), anyString());
    }
}