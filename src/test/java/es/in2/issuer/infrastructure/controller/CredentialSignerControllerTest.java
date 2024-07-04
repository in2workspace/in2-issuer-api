package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialSignerControllerTest {

    @Mock
    private CredentialSignerWorkflow credentialSignerWorkflow;

    @InjectMocks
    private CredentialSignerController credentialSignerController;

    @Test
    void itShouldReturnSignCredential() {
        String authorizationHeader = "Bearer testToken";
        String credentialId = "1";

        when(credentialSignerWorkflow.signCredential(authorizationHeader, credentialId)).thenReturn(Mono.empty());

        var result = credentialSignerController.createVerifiableCredential(authorizationHeader, credentialId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(credentialSignerWorkflow, times(1)).signCredential(authorizationHeader, credentialId);
        verifyNoMoreInteractions(credentialSignerWorkflow);
    }
}
