package es.in2.issuer.application.workflow.impl;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
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

    //TODO
   /* @Test
    void itS*/


    @Test
    void signCredentialOnRequestedFormat_JWT_Success() {
        String unsignedCredential = "unsignedCredential";
        String token = "dummyToken";
        String signedCredential = "signedJWTData";
        String procedureId = "procedureId";

        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token)))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES,signedCredential)));

        when(credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(unsignedCredential));
        when(deferredCredentialWorkflow.updateSignedCredentials(any(SignedCredentials.class))).thenReturn(Mono.empty());

        StepVerifier.create(credentialSignerWorkflow.signCredential(token, procedureId))
                .assertNext(signedData -> assertEquals(signedCredential, signedData))
                .verifyComplete();
    }
//    @Test
//    void signCredentialOnRequestedFormat_CWT_Success() {
//        String unsignedCredential = "{\"data\":\"data\"}";
//        String token = "dummyToken";
//        String signedCredential = "eyJkYXRhIjoiZGF0YSJ9";
//        String signedResult = "6BFWTLRH9.Q5$VAFLGV*M7:43S0";
//
//        when(remoteSignatureService.sign(any(SignatureRequest.class), eq(token)))
//                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, signedCredential)));
//
//
//        StepVerifier.create(credentialSignerWorkflow.signCredentialOnRequestedFormat(unsignedCredential, CWT_VC, token))
//                .assertNext(signedData -> assertEquals(signedResult, signedData))
//                .verifyComplete();
//    }
//
//    @Test
//    void signCredentialOnRequestedFormat_UnsupportedFormat() {
//        String unsignedCredential = "unsignedCredential";
//        String token = "dummyToken";
//        String unsupportedFormat = "unsupportedFormat";
//
//        StepVerifier.create(credentialSignerWorkflow.signCredentialOnRequestedFormat(unsignedCredential, unsupportedFormat, token))
//                .expectError(IllegalArgumentException.class)
//                .verify();
//    }


}