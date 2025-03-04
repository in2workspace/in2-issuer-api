package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
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

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private CredentialProcedureRepository credentialProcedureRepository;

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
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";

        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(
                authorizationHeader,
                procedureId,
                JWT_VC
        )).thenReturn(Mono.empty());
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(any())).thenReturn(Mono.empty());
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
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";

        // WHEN & THEN
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/retry-sign-credential/{procedure_id}").build(procedureId))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        // Verify that no interactions happened
        verifyNoInteractions(credentialSignerWorkflow);
        verifyNoInteractions(credentialProcedureService);
    }

}
