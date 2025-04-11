package es.in2.issuer.shared.application.workflow.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.shared.domain.model.dto.PendingCredentials;
import es.in2.issuer.shared.domain.model.dto.SignedCredentials;
import es.in2.issuer.shared.domain.model.entities.CredentialProcedure;
import es.in2.issuer.shared.domain.service.CredentialProcedureService;
import es.in2.issuer.shared.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.shared.domain.service.EmailService;
import es.in2.issuer.shared.infrastructure.repository.CredentialProcedureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DeferredCredentialWorkflowImplTest {
    @Mock
    private CredentialProcedureService credentialProcedureService;
    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeferredCredentialWorkflowImpl deferredCredentialWorkflow;

    @Mock
    private CredentialProcedureRepository credentialProcedureRepository;

    @Test
    void getPendingCredentialsByOrganizationId(){
        String organizationId = "4321";
        String expectedCredential = "Credential1";
        PendingCredentials expectedPendingCredentials = PendingCredentials.builder()
                .credentials(List.of(PendingCredentials.CredentialPayload.builder()
                                .credential(expectedCredential)
                        .build()))
                .build();

        when(credentialProcedureService.getAllIssuedCredentialByOrganizationIdentifier(organizationId)).thenReturn(Flux.just(expectedCredential));

        StepVerifier.create(deferredCredentialWorkflow.getPendingCredentialsByOrganizationId(organizationId))
                .expectNext(expectedPendingCredentials)
                .verifyComplete();
    }

    @Test
    void updateSignedCredentialsLearCredentialEmployee() throws JsonProcessingException {
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");
        credentialProcedure.setProcedureId(UUID.fromString(procedureId));
        String credential = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String expectedEmail = "juan.perez@mail.com";
        String expectedFirstName = "Juan";
        String expectedId = "390ecd06-4e56-483a-b550-18d93a4bf9e3";

        List<SignedCredentials.SignedCredential> credentials = List.of(SignedCredentials.SignedCredential.builder()
                .credential(credential)
                .build()
        );

        SignedCredentials signedCredentials = SignedCredentials.builder()
                .credentials(credentials)
                .build();

        String json = """
                {
                    "sub": "did:key:zDnaewZjPbFqyGvXVf5JuGCuSfTxXyFrKqoVrBFTQh17pqRbA",
                    "nbf": 1717579652,
                    "iss": "did:elsi:example",
                    "exp": 1720171652,
                    "iat": 1717579652,
                    "vc": {
                        "@context": [
                            "https://www.w3.org/ns/credentials/v2",
                            "https://dome-marketplace.eu/2022/credentials/learcredential/v1"
                        ],
                        "id": "390ecd06-4e56-483a-b550-18d93a4bf9e3",
                        "type": [
                            "LEARCredentialEmployee",
                            "VerifiableCredential"
                        ],
                        "credentialSubject": {
                            "mandate": {
                                "id": "836d631b-755b-4755-b3a9-30d21c2b001c",
                                "life_span": {
                                    "end_date_time": "2024-07-05T09:27:32.129565867Z",
                                    "start_date_time": "2024-06-05T09:27:32.129565867Z"
                                },
                                "mandatee": {
                                    "id": "did:key:zDnaewZjPbFqyGvXVf5JuGCuSfTxXyFrKqoVrBFTQh17pqRbA",
                                    "email": "juan.perez@mail.com",
                                    "firstName": "Juan",
                                    "lastName": "Perez",
                                    "mobile_phone": "+34 662233445"
                                },
                                "mandator": {
                                    "commonName": "IN2",
                                    "country": "ES",
                                    "emailAddress": "rrhh@in2.es",
                                    "organization": "IN2, Ingeniería de la Información, S.L.",
                                    "organizationIdentifier": "VATES-B60645900",
                                    "serialNumber": "B60645900"
                                },
                                "power": [
                                    {
                                        "id": "1483fd39-22ce-4e99-813e-331ed8bb5d79",
                                        "tmf_action": "Execute",
                                        "tmf_domain": "Dome",
                                        "tmf_function": "Onboarding",
                                        "tmf_type": "Domain"
                                    }
                                ]
                            }
                        },
                        "expirationDate": "2024-07-05T09:27:32.129565867Z",
                        "issuanceDate": "2024-06-05T09:27:32.129565867Z",
                        "issuer": "did:elsi:example",
                        "validFrom": "2024-06-05T09:27:32.129565867Z"
                    },
                    "jti": "d5576eb1-2184-42f0-93af-b14cf92a4e02"
                }
                """;
        ObjectMapper objectMapper2 = new ObjectMapper();
        JsonNode jsonNode = objectMapper2.readTree(json);

        when(objectMapper.readTree(anyString())).thenReturn(jsonNode);

        when(credentialProcedureService.updatedEncodedCredentialByCredentialId(signedCredentials.credentials().get(0).credential(),expectedId))
                .thenReturn(Mono.just(procedureId));

        when(deferredCredentialMetadataService.updateVcByProcedureId(credential,procedureId))
                .thenReturn(Mono.empty());

        when(emailService.sendCredentialSignedNotification(expectedEmail,"Credential Ready",expectedFirstName))
                .thenReturn(Mono.empty());

        when(deferredCredentialMetadataService.getOperationModeByProcedureId(procedureId)).thenReturn(Mono.just("A"));

        StepVerifier.create(deferredCredentialWorkflow.updateSignedCredentials(signedCredentials))
                .verifyComplete();
    }
}
