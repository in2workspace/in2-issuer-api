package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.PendingCredentials;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import es.in2.issuer.domain.service.CertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeferredCredentialControllerTest {

    @Mock
    private DeferredCredentialWorkflow deferredCredentialWorkflow;

    @Mock
    private CertificateService certificateService;

    @InjectMocks
    private DeferredCredentialController deferredCredentialController;

    @Test
    void getUnsignedCredentials() {
        // Arrange
        String organizationId = "testOrganizationId";
        PendingCredentials pendingCredentials = new PendingCredentials(List.of(new PendingCredentials.CredentialPayload("testCredentialId"))); // replace with actual data
        when(certificateService.getOrganizationIdFromCertificate(any())).thenReturn(Mono.just(organizationId));
        when(deferredCredentialWorkflow.getPendingCredentialsByOrganizationId(organizationId)).thenReturn(Mono.just(pendingCredentials));

        // Act
        Mono<PendingCredentials> result = deferredCredentialController.getUnsignedCredentials(MockServerWebExchange.from(MockServerHttpRequest.get("/")));

        // Assert
        StepVerifier.create(result)
                .assertNext(credentials -> assertEquals(pendingCredentials, credentials))
                .verifyComplete();
    }

    @Test
    void updateCredentials() {
        // Arrange
        SignedCredentials signedCredentials = new SignedCredentials(List.of(new SignedCredentials.SignedCredential("testCredentialId")));
        when(deferredCredentialWorkflow.updateSignedCredentials(signedCredentials)).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = deferredCredentialController.updateCredentials("clientCert", signedCredentials);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }
}