package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.domain.service.impl.NotificationServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private final String processId = "processId";
    private final String procedureId = "procedureId";
    private final String email = "test@example.com";
    private final String user = "Jhon";
    private final String knowledgebaseUrl = "http://knowledgebaseUrl.com";
    private final String organization = "organization";
    private final String issuerUiExternalDomain = "http://example.com";

    @Mock
    private AppConfig appConfig;
    @Mock
    private EmailService emailService;
    @Mock
    private CredentialProcedureService credentialProcedureService;
    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;
    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setup() {
        lenient().when(appConfig.getIssuerUiExternalDomain()).thenReturn(issuerUiExternalDomain);
    }

    @Test
    void testSendNotification_WithWithdrawnStatus() {
        String transactionCode = "transactionCode";
        when(credentialProcedureService.getCredentialStatusByProcedureId(procedureId))
                .thenReturn(Mono.just(CredentialStatus.WITHDRAWN.toString()));
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(email));
        when(credentialProcedureService.getMandateeCompleteNameFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(user));
        when(credentialProcedureService.getMandatorOrganizationFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(organization));
        when(deferredCredentialMetadataService.updateTransactionCodeInDeferredCredentialMetadata(procedureId))
                .thenReturn(Mono.just(transactionCode));
        when(appConfig.getKnowledgebaseUrl()).thenReturn(knowledgebaseUrl);
        when(emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer",
                issuerUiExternalDomain + "/credential-offer?transaction_code=" + transactionCode,knowledgebaseUrl, user,organization))
                .thenReturn(Mono.empty());

        Mono<Void> result = notificationService.sendNotification(processId, procedureId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(emailService, times(1)).sendTransactionCodeForCredentialOffer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testSendNotification_WithPendDownloadStatus() {
        when(credentialProcedureService.getCredentialStatusByProcedureId(procedureId))
                .thenReturn(Mono.just(CredentialStatus.PEND_DOWNLOAD.toString()));
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(email));
        when(credentialProcedureService.getMandateeCompleteNameFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(user));
        when(credentialProcedureService.getMandatorOrganizationFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(organization));
        when(emailService.sendCredentialSignedNotification(email, "Credential Ready", user))
                .thenReturn(Mono.empty());

        Mono<Void> result = notificationService.sendNotification(processId, procedureId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(emailService, times(1)).sendCredentialSignedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void testSendNotification_WithUnhandledStatus() {
        when(credentialProcedureService.getCredentialStatusByProcedureId(procedureId))
                .thenReturn(Mono.just("UNHANDLED_STATUS"));
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(email));
        when(credentialProcedureService.getMandateeCompleteNameFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(user));
        when(credentialProcedureService.getMandatorOrganizationFromDecodedCredentialByProcedureId(procedureId))
                .thenReturn(Mono.just(organization));

        Mono<Void> result = notificationService.sendNotification(processId, procedureId);

        StepVerifier.create(result)
                .verifyComplete();

        verify(emailService, never()).sendTransactionCodeForCredentialOffer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(emailService, never()).sendCredentialSignedNotification(anyString(), anyString(), anyString());
    }
}