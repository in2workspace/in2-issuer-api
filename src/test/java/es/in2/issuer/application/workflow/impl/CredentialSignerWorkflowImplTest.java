package es.in2.issuer.application.workflow.impl;

import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.domain.util.factory.VerifiableCertificationFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialWorkflow deferredCredentialWorkflow;

    @InjectMocks
    CredentialSignerWorkflowImpl credentialSignerWorkflow;

    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @Mock
    private VerifiableCertificationFactory verifiableCertificationFactory;

    @Test
    void signCredentialOnRequestedFormat_JWT_Success() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String signedCredential = "signedJWTData";
        String procedureId = "procedureId";

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(unsignedCredential)).thenReturn(LEARCredentialEmployee .builder().build());
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(any(LEARCredentialEmployee.class)))
                .thenReturn(Mono.just(LEARCredentialEmployeeJwtPayload.builder().build()));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedCredential));

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(unsignedCredential));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

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
        String procedureId = "procedureId";

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq("")))
                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, signedCredential)));

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(unsignedCredential)).thenReturn(LEARCredentialEmployee .builder().build());
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(any(LEARCredentialEmployee.class)))
                .thenReturn(Mono.just(LEARCredentialEmployeeJwtPayload.builder().build()));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedCredential));

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(unsignedCredential));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, CWT_VC))
                .assertNext(signedData -> assertEquals(signedResult, signedData))
                .verifyComplete();
    }

    @Test
    void signCredentialOnRequestedFormat_UnsupportedFormat() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String unsupportedFormat = "unsupportedFormat";
        String procedureId = "procedureId";

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(unsignedCredential));

        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, unsupportedFormat))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void signCredentialWithVcAlreadyPresent_Success() {
        String unsignedCredential = "{\"vc\":{\"id\":\"123\"}}"; // Contiene "vc"
        String signedCredential = "signedJWTData";
        String token = "dummyToken";
        String procedureId = "procedureId";

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(unsignedCredential));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

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
        String procedureId = "procedureId";

        VerifiableCertification mockCertification = VerifiableCertification.builder().build();
        VerifiableCertificationJwtPayload mockVerifiablePayload = mock(VerifiableCertificationJwtPayload.class);

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(decodedCredential));
        when(verifiableCertificationFactory.mapStringToVerifiableCertification(decodedCredential)).thenReturn(mockCertification);
        when(verifiableCertificationFactory.buildVerifiableCertificationJwtPayload(mockCertification)).thenReturn(Mono.just(mockVerifiablePayload));
        when(verifiableCertificationFactory.convertVerifiableCertificationJwtPayloadInToString(any(VerifiableCertificationJwtPayload.class)))
                .thenReturn(Mono.just(unsignedJwtPayload));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

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
        String procedureId = "procedureId";

        LEARCredentialEmployee mockEmployee = LEARCredentialEmployee.builder().build();
        LEARCredentialEmployeeJwtPayload mockLearPayload = mock(LEARCredentialEmployeeJwtPayload.class);

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(decodedCredential));
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(decodedCredential)).thenReturn(mockEmployee);
        when(learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(mockEmployee)).thenReturn(Mono.just(mockLearPayload));
        when(learCredentialEmployeeFactory.convertLEARCredentialEmployeeJwtPayloadInToString(any(LEARCredentialEmployeeJwtPayload.class)))
                .thenReturn(Mono.just(unsignedJwtPayload));
        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, signedCredential)));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

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
        String procedureId = "procedureId";

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(decodedCredential));
        when(verifiableCertificationFactory.mapStringToVerifiableCertification(decodedCredential))
                .thenThrow(new RuntimeException("Mapping error"));

        StepVerifier.create(credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(verifiableCertificationFactory).mapStringToVerifiableCertification(decodedCredential);
        verify(remoteSignatureService, never()).sign(any(SignatureRequest.class), eq(token), eq(procedureId));
        verify(deferredCredentialWorkflow, never()).updateSignedCredentials(any(SignedCredentials.class));
    }




}