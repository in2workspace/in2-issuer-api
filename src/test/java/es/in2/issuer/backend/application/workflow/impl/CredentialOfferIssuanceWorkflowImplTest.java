package es.in2.issuer.backend.application.workflow.impl;

import es.in2.issuer.authserver.application.workflow.PreAuthCodeWorkflow;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.backend.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.backend.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.backend.domain.service.CredentialProcedureService;
import es.in2.issuer.backend.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.backend.domain.service.EmailService;
import es.in2.issuer.backend.domain.service.impl.CredentialOfferServiceImpl;
import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import es.in2.issuer.shared.objectmother.PreAuthCodeResponseMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferIssuanceWorkflowImplTest {

    @Mock
    private CredentialOfferServiceImpl credentialOfferService;

    @Mock
    private CredentialOfferCacheStorageService credentialOfferCacheStorageService;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Mock
    private EmailService emailService;

    @Mock
    private PreAuthCodeWorkflow preAuthCodeWorkflow;

    @InjectMocks
    private CredentialOfferIssuanceWorkflowImpl credentialOfferIssuanceService;

    @Test
    void testGetCredentialOffer() {
        String id = "dummyId";
        String email = "example@example.com";
        String pin = "1234";
        CustomCredentialOffer credentialOffer = CustomCredentialOffer.builder().build();
        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(credentialOffer)
                .pin(pin)
                .employeeEmail(email)
                .build();

        when(credentialOfferCacheStorageService.getCustomCredentialOffer(id)).thenReturn(Mono.just(credentialOfferData));
        when(emailService.sendPin(credentialOfferData.employeeEmail(), "Pin Code", credentialOfferData.pin())).thenReturn(Mono.empty());

        Mono<CustomCredentialOffer> result = credentialOfferIssuanceService.getCustomCredentialOffer(id);
        assertEquals(credentialOffer, result.block());
    }

    @Test
    void testBuildCredentialOfferUri() {
        String processId = "1234";
        String transactionCode = "4321";
        String procedureId = "uuid1234";
        String credentialType = "VerifiableCredential";
        String nonce = "nonce";
        String credentialOfferUri = "https://example.com/1234";
        String mail = "user@gmail.com";

        PreAuthCodeResponse preAuthCodeResponse =
                PreAuthCodeResponseMother.withPreAuthCodeAndPin("4567", transactionCode);

        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(CustomCredentialOffer.builder().build())
                .pin(transactionCode)
                .employeeEmail(mail)
                .build();

        String cTransactionCode = "cTransactionCode";
        int expiry = 1000;
        Map<String, Object> cTransactionCodeMap = new HashMap<>();
        cTransactionCodeMap.put("cTransactionCode", cTransactionCode);
        cTransactionCodeMap.put("cTransactionCodeExpiresIn", expiry);

        when(deferredCredentialMetadataService.validateTransactionCode(transactionCode))
                .thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode))
                .thenReturn(Mono.just(procedureId));
        when(preAuthCodeWorkflow.generatePreAuthCodeResponse())
                .thenReturn(Mono.just(preAuthCodeResponse));
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId))
                .thenReturn(Mono.just(credentialType));

        when(deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(
                transactionCode, preAuthCodeResponse.grant().preAuthorizedCode()))
                .thenReturn(Mono.empty());
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(mail));
        when(credentialOfferService.buildCustomCredentialOffer(
                credentialType,
                preAuthCodeResponse.grant(),
                mail,
                preAuthCodeResponse.pin()))
                .thenReturn(Mono.just(credentialOfferData));
        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOfferData))
                .thenReturn(Mono.just(nonce));
        when(credentialOfferService.createCredentialOfferUriResponse(nonce))
                .thenReturn(Mono.just(credentialOfferUri));
        when(deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(transactionCode))
                .thenReturn(Mono.just(cTransactionCodeMap));

        CredentialOfferUriResponse expectedResponse = CredentialOfferUriResponse.builder()
                .credentialOfferUri(credentialOfferUri)
                .cTransactionCode(cTransactionCode)
                .cTransactionCodeExpiresIn(expiry)
                .build();

        StepVerifier.create(credentialOfferIssuanceService.buildCredentialOfferUri(processId, transactionCode))
                .expectNext(expectedResponse)
                .verifyComplete();
    }


    @Test
    void testBuildNewCredentialOfferUri() {
        String processId = "1234";
        String subTransactionCode = "9876";
        String originalTransactionCode = "4321";
        String procedureId = "uuid1234";
        String credentialType = "VerifiableCredential";

        String nonce = "nonce";
        String credentialOfferUri = "https://example.com/1234";
        String mail = "user@gmail.com";

        PreAuthCodeResponse preAuthCodeResponse =
                PreAuthCodeResponseMother.withPreAuthCodeAndPin("4567", subTransactionCode);

        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(CustomCredentialOffer.builder().build())
                .pin(subTransactionCode)
                .employeeEmail(mail)
                .build();

        // Se simula el Map con los detalles de cTransactionCode
        String cTransactionCode = "cTransactionCode";
        int expiry = 1000;
        Map<String, Object> cTransactionCodeMap = new HashMap<>();
        cTransactionCodeMap.put("cTransactionCode", cTransactionCode);
        cTransactionCodeMap.put("cTransactionCodeExpiresIn", expiry);

        // Stubbing
        when(deferredCredentialMetadataService.validateCTransactionCode(subTransactionCode))
                .thenReturn(Mono.just(originalTransactionCode));
        when(deferredCredentialMetadataService.getProcedureIdByTransactionCode(originalTransactionCode))
                .thenReturn(Mono.just(procedureId));
        when(preAuthCodeWorkflow.generatePreAuthCodeResponse())
                .thenReturn(Mono.just(preAuthCodeResponse));
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId))
                .thenReturn(Mono.just(credentialType));

        when(deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(
                originalTransactionCode, preAuthCodeResponse.grant().preAuthorizedCode()))
                .thenReturn(Mono.empty());
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(mail));
        when(credentialOfferService.buildCustomCredentialOffer(
                credentialType,
                preAuthCodeResponse.grant(),
                mail,
                preAuthCodeResponse.pin()))
                .thenReturn(Mono.just(credentialOfferData));
        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOfferData))
                .thenReturn(Mono.just(nonce));
        when(credentialOfferService.createCredentialOfferUriResponse(nonce))
                .thenReturn(Mono.just(credentialOfferUri));
        when(deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(originalTransactionCode))
                .thenReturn(Mono.just(cTransactionCodeMap));

        CredentialOfferUriResponse expectedResponse = CredentialOfferUriResponse.builder()
                .credentialOfferUri(credentialOfferUri)
                .cTransactionCode(cTransactionCode)
                .cTransactionCodeExpiresIn(expiry)
                .build();

        StepVerifier.create(credentialOfferIssuanceService.buildNewCredentialOfferUri(processId, subTransactionCode))
                .expectNext(expectedResponse)
                .verifyComplete();
    }

}