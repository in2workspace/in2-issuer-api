package es.in2.issuer.backend.infrastructure.controller;

import es.in2.issuer.backend.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.backend.domain.model.dto.AuthServerNonceRequest;
import es.in2.issuer.backend.infrastructure.controller.DeferredCredentialMetadataController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeferredCredentialMetadataControllerTest {

    @Mock
    private VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;

    @InjectMocks
    private DeferredCredentialMetadataController deferredCredentialMetadataController;

    @Test
    void bindAccessTokenByPreAuthorizedCode() {
        // Arrange
        AuthServerNonceRequest authServerNonceRequest = new AuthServerNonceRequest("pre-authorized-code", "access-token");
        when(verifiableCredentialIssuanceWorkflow.bindAccessTokenByPreAuthorizedCode(anyString(), eq(authServerNonceRequest))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = deferredCredentialMetadataController.bindAccessTokenByPreAuthorizedCode(authServerNonceRequest);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }
}