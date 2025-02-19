package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.model.dto.ProcedureIdRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.mockito.Mockito.*;
import static es.in2.issuer.domain.util.Constants.JWT_VC;

@ExtendWith(MockitoExtension.class)
class SignUnsignedCredentialControllerTest {

    @Mock
    private CredentialSignerWorkflow credentialSignerWorkflow;

    @InjectMocks
    private SignUnsignedCredentialController signUnsignedCredentialController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(signUnsignedCredentialController).build();
    }

    @Test
    void testSignUnsignedCredential_Success() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "procedure-123";

        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(
                authorizationHeader,
                procedureId,
                JWT_VC
        )).thenReturn(Mono.empty());

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectComplete()
                .verify();

        verify(credentialSignerWorkflow, times(1))
                .signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);
    }

    @Test
    void shouldReturnUnauthorizedWhenAuthorizationHeaderIsMissing() {
        // GIVEN
        String procedureId= "procedure-123";

        // WHEN & THEN
        webTestClient.post()
                .uri("/api/v1/retry-sign-credential/procedureId")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify that the service method was never called
        verifyNoInteractions(credentialSignerWorkflow);
    }
}
