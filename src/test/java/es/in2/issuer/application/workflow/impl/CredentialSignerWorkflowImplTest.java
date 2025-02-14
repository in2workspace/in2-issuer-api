package es.in2.issuer.application.workflow.impl;

import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.RemoteSignatureService;
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
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class CredentialSignerWorkflowImplTest {
    private static final Logger logger = LoggerFactory.getLogger(CredentialSignerWorkflowImplTest.class);
    @Mock
    private RemoteSignatureService remoteSignatureService;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialWorkflow deferredCredentialWorkflow;

    @InjectMocks
    CredentialSignerWorkflowImpl credentialSignerWorkflow;

    @Test
    void signCredentialOnRequestedFormat_JWT_Success() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String signedCredential = "signedJWTData";
        String procedureId = "procedureId";

        logger.info("Este es un log de prueba en JUnit");

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token), eq(procedureId)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));

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


}