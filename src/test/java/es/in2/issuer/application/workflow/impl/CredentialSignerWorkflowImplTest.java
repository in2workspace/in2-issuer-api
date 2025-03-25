package es.in2.issuer.application.workflow.impl;

import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.domain.util.factory.VerifiableCertificationFactory;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.CWT_VC;
import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialSignerWorkflowImplTest {
    @Mock
    private RemoteSignatureService remoteSignatureService;

    @Mock
    private DeferredCredentialWorkflow deferredCredentialWorkflow;

    @Spy
    @InjectMocks
    CredentialSignerWorkflowImpl credentialSignerWorkflow;

    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @Mock
    private VerifiableCertificationFactory verifiableCertificationFactory;

    @Mock
    CredentialProcedureRepository credentialProcedureRepository;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    private final String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";
    private final String authorizationHeader = "Bearer some-token";
    private final String bindedCredential = "bindedCredential";

    @Test
    void signCredentialOnRequestedFormat_JWT_Success() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String signedCredential = "signedJWTData";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(unsignedCredential);
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(unsignedCredential)).thenReturn(LEARCredentialEmployee .builder().build());
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(any(LEARCredentialEmployee.class)))
                .thenReturn(Mono.just(LEARCredentialEmployeeJwtPayload.builder().build()));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedCredential));

        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .assertNext(signedData -> assertEquals(signedCredential, signedData))
                .verifyComplete();
    }
    @Test
    void signCredentialOnRequestedFormat_CWT_Success() {
        String unsignedCredential = "{\"data\":\"data\"}";
        String token = "dummyToken";
        String signedCredential = "eyJkYXRhIjoiZGF0YSJ9";
        String signedResult = "6BFWTLRH9.Q5$VAFLGV*M7:43S0";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(unsignedCredential);
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq("")))
                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, signedCredential)));

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(unsignedCredential)).thenReturn(LEARCredentialEmployee .builder().build());
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(any(LEARCredentialEmployee.class)))
                .thenReturn(Mono.just(LEARCredentialEmployeeJwtPayload.builder().build()));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedCredential));
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, CWT_VC))
                .assertNext(signedData -> assertEquals(signedResult, signedData))
                .verifyComplete();
    }

    @Test
    void signCredentialOnRequestedFormat_UnsupportedFormat() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String unsupportedFormat = "unsupportedFormat";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(unsignedCredential);
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, unsupportedFormat))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void signCredentialWithVcAlreadyPresent_Success() {
        String unsignedCredential = "{\"vc\":{\"id\":\"123\"}}"; // Contiene "vc"
        String signedCredential = "signedJWTData";
        String token = "dummyToken";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(unsignedCredential);
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .assertNext(result -> assertEquals(signedCredential, result))
                .verifyComplete();

        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token), eq(procedureId));
        verify(deferredCredentialWorkflow).updateSignedCredentials(any(SignedCredentials.class));
    }

    @Test
    void signVerifiableCertificationCredential_Success() {
        String decodedCredential = "{\"VerifiableCertification\":\"data\"}";
        String unsignedJwtPayload = "unsignedPayload";
        String signedCredential = "signedJWTData";
        String token = "dummyToken";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(decodedCredential);
        credentialProcedure.setCredentialType("VERIFIABLE_CERTIFICATION");
        VerifiableCertification mockCertification = VerifiableCertification.builder().build();
        VerifiableCertificationJwtPayload mockVerifiablePayload = mock(VerifiableCertificationJwtPayload.class);

        when(verifiableCertificationFactory.mapStringToVerifiableCertification(decodedCredential)).thenReturn(mockCertification);
        when(verifiableCertificationFactory.buildVerifiableCertificationJwtPayload(mockCertification)).thenReturn(Mono.just(mockVerifiablePayload));
        when(verifiableCertificationFactory.convertVerifiableCertificationJwtPayloadInToString(any(VerifiableCertificationJwtPayload.class)))
                .thenReturn(Mono.just(unsignedJwtPayload));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .assertNext(result -> assertEquals(signedCredential, result))
                .verifyComplete();

        verify(verifiableCertificationFactory).mapStringToVerifiableCertification(decodedCredential);
        verify(verifiableCertificationFactory).buildVerifiableCertificationJwtPayload(any(VerifiableCertification.class));
        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token), eq(procedureId));
        verify(deferredCredentialWorkflow).updateSignedCredentials(any(SignedCredentials.class));
    }

    @Test
    void signLEARCredentialEmployee_Success() {
        String decodedCredential = "{\"LEARCredentialEmployee\":\"data\"}";
        String unsignedJwtPayload = "unsignedPayload";
        String signedCredential = "signedJWTData";
        String token = "dummyToken";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(decodedCredential);
        credentialProcedure.setCredentialType("LEAR_CREDENTIAL_EMPLOYEE");

        LEARCredentialEmployee mockEmployee = LEARCredentialEmployee.builder().build();
        LEARCredentialEmployeeJwtPayload mockLearPayload = mock(LEARCredentialEmployeeJwtPayload.class);

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(decodedCredential)).thenReturn(mockEmployee);
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(mockEmployee)).thenReturn(Mono.just(mockLearPayload));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedJwtPayload));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .assertNext(result -> assertEquals(signedCredential, result))
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapStringToLEARCredentialEmployee(decodedCredential);
        verify(learCredentialEmployeeFactory).buildLEARCredentialEmployeeJwtPayload(any(LEARCredentialEmployee.class));
        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token), eq(procedureId));
        verify(deferredCredentialWorkflow).updateSignedCredentials(any(SignedCredentials.class));
    }

    @Test
    void signCredential_ErrorDuringJwtPayloadCreation() {
        String decodedCredential = "{\"VerifiableCertification\":\"data\"}";
        String token = "dummyToken";
        String procedureId = UUID.randomUUID().toString();
        CredentialProcedure credentialProcedure = new CredentialProcedure();
        credentialProcedure.setCredentialDecoded(decodedCredential);
        credentialProcedure.setCredentialType("VERIFIABLE_CERTIFICATION");

        when(verifiableCertificationFactory.mapStringToVerifiableCertification(decodedCredential))
                .thenThrow(new RuntimeException("Mapping error"));
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(credentialProcedure));
        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(verifiableCertificationFactory).mapStringToVerifiableCertification(decodedCredential);
        verify(remoteSignatureService, never()).sign(any(SignatureRequest.class), eq(token), eq(procedureId));
        verify(deferredCredentialWorkflow, never()).updateSignedCredentials(any(SignedCredentials.class));
    }
    @Test
    void testRetrySignUnsignedCredential_Success() {
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.just(bindedCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC))
                .thenReturn(Mono.empty());
        doReturn(Mono.just("true"))
                .when(credentialSignerWorkflow)
                .signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(credentialProcedureRepository.save(any())).thenReturn(Mono.just(credentialProcedure));

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .verifyComplete();

        verify(learCredentialEmployeeFactory).mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId);
        verify(credentialProcedureService).updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC);
        verify(credentialSignerWorkflow).signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);
        verify(credentialProcedureService).updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId);
        verify(credentialProcedureRepository, times(2)).findByProcedureId(UUID.fromString(procedureId));
        verify(credentialProcedureRepository).save(any());
    }

    @Test
    void testRetrySignUnsignedCredential_ThrowsWhenProcedureNotFound() {
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.empty());

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .expectErrorMessage("Procedure not found")
                .verify();
    }

    @Test
    void testRetrySignUnsignedCredential_ErrorOnMappingCredential() {
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.error(new RuntimeException("Mapping failed")));

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .expectErrorMessage("Mapping failed")
                .verify();
    }

    @Test
    void testRetrySignUnsignedCredential_ErrorOnUpdatingDecodedCredential() {
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential("decodedCredential", procedureId))
                .thenReturn(Mono.just(bindedCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC))
                .thenReturn(Mono.error(new RuntimeException("Failed to update decoded credential")));

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .expectErrorMessage("Failed to update decoded credential")
                .verify();
    }

    @Test
    void testRetrySignUnsignedCredential_ErrorOnSigningCredential() {
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(any(), eq(procedureId)))
                .thenReturn(Mono.just(bindedCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC))
                .thenReturn(Mono.empty());
        doReturn(Mono.error(new RuntimeException("Signing failed")))
                .when(credentialSignerWorkflow)
                .signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .expectErrorMessage("Signing failed")
                .verify();
    }

    @Test
    void testRetrySignUnsignedCredential_ErrorOnSavingUpdatedAt() {
        CredentialProcedure credentialProcedure = mock(CredentialProcedure.class);
        when(credentialProcedure.getCredentialDecoded()).thenReturn("decodedCredential");

        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(any(), eq(procedureId)))
                .thenReturn(Mono.just(bindedCredential));
        when(credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindedCredential, JWT_VC))
                .thenReturn(Mono.empty());
        doReturn(Mono.just("signedCredential"))
                .when(credentialSignerWorkflow)
                .signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC);
        when(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .thenReturn(Mono.empty());
        when(credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId)))
                .thenReturn(Mono.just(credentialProcedure));
        when(credentialProcedureRepository.save(any()))
                .thenReturn(Mono.error(new RuntimeException("Failed to update updatedAt")));

        StepVerifier.create(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .expectErrorMessage("Failed to update updatedAt")
                .verify();
    }

}