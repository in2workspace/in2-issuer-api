package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.exception.RemoteSignatureException;
import es.in2.issuer.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.domain.model.dto.VerifiableCredentialResponse;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.EmailService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;

import static es.in2.issuer.domain.util.Constants.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final CredentialFactory credentialFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final EmailService emailService;
    private final AppConfig appConfig;

    @Override
    public Mono<String> generateVc(String processId, String vcType, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, preSubmittedCredentialRequest, token)
                .flatMap(credentialProcedureService::createCredentialProcedure)
                .flatMap(procedureId -> deferredCredentialMetadataService.createDeferredCredentialMetadata(
                        procedureId,
                        preSubmittedCredentialRequest.operationMode(),
                        preSubmittedCredentialRequest.responseUri()));
    }

    @Override
    public Mono<String> generateVerifiableCertification(String processId, String vcType, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, preSubmittedCredentialRequest, token)
                .flatMap(credentialProcedureService::createCredentialProcedure);
    }

    @Override
    public Mono<VerifiableCredentialResponse> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest) {
        return deferredCredentialMetadataService.getVcByTransactionId(deferredCredentialRequest.transactionId())
                .flatMap(deferredCredentialMetadataDeferredResponse -> {
                    if (deferredCredentialMetadataDeferredResponse.vc() != null) {
                        return credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(deferredCredentialMetadataDeferredResponse.procedureId())
                                .then(deferredCredentialMetadataService.deleteDeferredCredentialMetadataById(deferredCredentialMetadataDeferredResponse.id()))
                                .then(Mono.just(VerifiableCredentialResponse.builder()
                                        .credential(deferredCredentialMetadataDeferredResponse.vc())
                                        .build()));
                    } else {
                        return Mono.just(VerifiableCredentialResponse.builder()
                                .transactionId(deferredCredentialMetadataDeferredResponse.transactionId())
                                .build());
                    }
                });
    }

    @Override
    public Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode) {
        try {
            JWSObject jwsObject = JWSObject.parse(accessToken);
            String newAuthServerNonce = jwsObject.getPayload().toJSONObject().get("jti").toString();
            return deferredCredentialMetadataService.updateAuthServerNonceByAuthServerNonce(newAuthServerNonce, preAuthCode);
        } catch (ParseException e){
            throw new RuntimeException();
        }

    }

    @Override
    public Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format, String token, String operationMode) {
        return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
            .flatMap(procedureId -> {
                log.info("Procedure ID obtained: {}", procedureId);
                return credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                        .flatMap(credentialType -> {
                            log.info("Credential Type obtained: {}", credentialType);
                            return credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)
                                    .flatMap(decodedCredential -> {
                                        log.info("Decoded Credential obtained: {}", decodedCredential);
                                        return credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, decodedCredential, subjectDid)
                                                .flatMap(bindCredentialWithMandateeId -> credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredentialWithMandateeId, format)
                                                    .then(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format))
                                                    .flatMap(transactionIdMandatee -> {
                                                        log.info("Transaction ID obtained for Mandatee Completion: {}", transactionIdMandatee);
                                                        return credentialFactory.mapCredentialBindIssuerAndUpdateDB(processId, procedureId, bindCredentialWithMandateeId, credentialType, format, authServerNonce)
                                                                .flatMap(transactionIdIssuer -> {
                                                                    log.info("Transaction ID obtained for Issuer Completion: {}", transactionIdIssuer);
                                                                    return buildCredentialResponseBasedOnOperationMode(operationMode, bindCredentialWithMandateeId, transactionIdIssuer, authServerNonce, token);
                                                                });
                                                    }));
                                    });
                        });
            });
    }



    private Mono<VerifiableCredentialResponse> buildCredentialResponseBasedOnOperationMode(String operationMode, String bindCredential, String transactionId, String authServerNonce, String token) {
        if (operationMode.equals(ASYNC)) {
            log.info("LEAR Credential JSON: {}", bindCredential);
            return Mono.just(VerifiableCredentialResponse.builder()
                    .credential(bindCredential)
                    .transactionId(transactionId)
                    .build());
        } else if (operationMode.equals(SYNC)) {
            String domain = appConfig.getIssuerUiExternalDomain();
            return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                    .flatMap(procedureIdReceived ->
                            credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + token, procedureIdReceived, JWT_VC)
                                    .flatMap(signedCredential -> Mono.just(VerifiableCredentialResponse.builder()
                                            .credential(signedCredential)
                                            .build()))
                                    .onErrorResume(RemoteSignatureException.class, error -> {
                                        log.info("Error in SYNC mode, retrying with new operation mode");
                                        return credentialProcedureService.getSignerEmailFromDecodedCredentialByProcedureId(procedureIdReceived)
                                                .flatMap(signerEmail ->
                                                        emailService.sendPendingSignatureCredentialNotification(signerEmail, "Failed to sign credential, please activate manual signature.", procedureIdReceived, domain)
                                                                .then(credentialProcedureService.getDecodedCredentialByProcedureId(procedureIdReceived))
                                                )
                                                .flatMap(unsignedCredential -> Mono.just(VerifiableCredentialResponse.builder()
                                                        .credential(unsignedCredential)
                                                        .transactionId(transactionId)
                                                        .build()));
                                    })
                    );
        }
        else {
            return Mono.error(new IllegalArgumentException("Unknown operation mode: " + operationMode));
        }
    }

}
