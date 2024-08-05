package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.EmailService;
import es.in2.issuer.domain.service.NotificationService;
import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.model.enums.CredentialStatus.PEND_DOWNLOAD;
import static es.in2.issuer.domain.model.enums.CredentialStatus.WITHDRAWN;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final AppConfig appConfig;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<Void> sendNotification(String processId, String procedureId) {
        return credentialProcedureService.getCredentialStatusByProcedureId(procedureId)
                .flatMap(status -> credentialProcedureService.getCredentialSubjectEmailFromDecodedCredentialByProcedureId(procedureId)
                        .flatMap(email -> credentialProcedureService.getCredentialSubjectNameFromDecodedCredentialByProcedureId(procedureId)
                                .flatMap(name -> {
                                    if (status.equals(WITHDRAWN.toString())) {
                                        return deferredCredentialMetadataService.updateTransactionCodeInDeferredCredentialMetadata(procedureId)
                                                .flatMap(newTransactionCode -> emailService.sendTransactionCodeForCredentialOffer(
                                                        email,
                                                        "Credential Offer",
                                                        appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + newTransactionCode,
                                                        name,
                                                        appConfig.getWalletUrl()
                                                ));
                                    } else if (status.equals(PEND_DOWNLOAD.toString())) {
                                        return emailService.sendCredentialSignedNotification(email, "Credential Ready", name);
                                    } else {
                                        return Mono.empty();
                                    }
                                })
                        )
                );
    }
}
