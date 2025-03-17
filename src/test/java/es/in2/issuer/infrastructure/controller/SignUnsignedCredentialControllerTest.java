package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
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

    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(signUnsignedCredentialController).build();
    }

    @Test
    void testSignUnsignedCredential_Success() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
        String bindedCredential = "bindedCredential";

        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(any()))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.just(bindedCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC))
                .thenReturn(Mono.just(true).then());
        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC))
                .thenReturn(Mono.just("true"));
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.just(true).then());
        when(credentialProcedureRepository.save(any()))
                .thenReturn(Mono.just(credentialProcedure));

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectComplete()
                .verify();
        verify(learCredentialEmployeeFactory, times(1)).mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId);
        verify(credentialProcedureService, times(1)).updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC);
        verify(credentialSignerWorkflow, times(1)).signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);
        verify(credentialProcedureService, times(1)).updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId);
        verify(credentialProcedureRepository, times(2)).findByProcedureId(any());
        verify(credentialProcedureRepository, times(1)).save(any());
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

    @Test
    void testSignUnsignedCredential_ErrorOnMappingCredential() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);

        when(credentialProcedureRepository.findByProcedureId(any())).thenReturn(Mono.just(credentialProcedure));
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.error(new RuntimeException("Mapping failed")));

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectError(RuntimeException.class)
                .verify();

        verify(learCredentialEmployeeFactory, times(1)).mapCredentialAndBindIssuerInToTheCredential(any(), eq(procedureId));
    }

    @Test
    void testSignUnsignedCredential_ErrorOnUpdatingDecodedCredential() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);

        when(credentialProcedureRepository.findByProcedureId(any())).thenReturn(Mono.just(credentialProcedure));
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.just("bindedCredential"));

        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, "bindedCredential", JWT_VC))
                .thenReturn(Mono.error(new RuntimeException("Failed to update decoded credential")));

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testSignUnsignedCredential_ErrorOnSigningCredential() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);

        when(credentialProcedureRepository.findByProcedureId(any())).thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(any(), eq(procedureId)))
                .thenReturn(Mono.just("bindedCredential"));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, "bindedCredential", JWT_VC))
                .thenReturn(Mono.empty());
        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC))
                .thenReturn(Mono.error(new RuntimeException("Signing failed")));

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testSignUnsignedCredential_ErrorOnSavingUpdatedAt() {
        String authorizationHeader = "Bearer some-token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);

        when(credentialProcedureRepository.findByProcedureId(any())).thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(any(), eq(procedureId)))
                .thenReturn(Mono.just("bindedCredential"));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, "bindedCredential", JWT_VC))
                .thenReturn(Mono.empty());
        when(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC))
                .thenReturn(Mono.just("true"));
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.empty());

        when(credentialProcedureRepository.save(any())).thenReturn(Mono.error(new RuntimeException("Failed to update updatedAt")));

        Mono<Void> response = signUnsignedCredentialController.signUnsignedCredential(authorizationHeader, procedureId);

        StepVerifier.create(response)
                .expectError(RuntimeException.class)
                .verify();
    }
}
