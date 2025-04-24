package es.in2.issuer.backoffice.application.workflow.impl;

import es.in2.issuer.oidc4vci.application.workflow.PreAuthorizedCodeWorkflow;
import es.in2.issuer.backoffice.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.backoffice.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.backoffice.domain.service.*;
import es.in2.issuer.shared.domain.service.CredentialProcedureService;
import es.in2.issuer.shared.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.shared.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceWorkflowImpl implements CredentialOfferIssuanceWorkflow {

    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final PreAuthorizedCodeWorkflow preAuthorizedCodeWorkflow;

    @Override
    public Mono<CredentialOfferUriResponse> buildCredentialOfferUri(String processId, String transactionCode) {
        return deferredCredentialMetadataService.validateTransactionCode(transactionCode)
                .then(Mono.just(transactionCode))
                .flatMap(this::buildCredentialOfferUriInternal);
    }

    @Override
    public Mono<CredentialOfferUriResponse> buildNewCredentialOfferUri(String processId, String cTransactionCode) {
        return deferredCredentialMetadataService.validateCTransactionCode(cTransactionCode)
                .flatMap(this::buildCredentialOfferUriInternal);
    }

    // Add logs to debug the process
    private Mono<CredentialOfferUriResponse> buildCredentialOfferUriInternal(String transactionCode) {
        // Get ProcedureId (IssuanceRecordId) by transaction_code (activation_code)
        return deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                .flatMap(procedureId ->
                                // Get CredentialProcedure (IssuanceRecord) by procedureId (IssuanceRecordId)
                                credentialProcedureService.getCredentialProcedureById(procedureId)
                                .flatMap(credentialProcedure ->
                                        preAuthorizedCodeWorkflow.generatePreAuthorizedCode(Mono.just(credentialProcedure.getCredentialId()))
                                                .flatMap(preAuthorizedCodeResponse ->
                                                        // buildCredentialOffer(credentialProcedure.getCredentialType(), preAuthorizedCodeResponse.grant())
                                                        // --> CredentialOffer
                                                        // credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId)
                                                        // --> CredentialOwnerEmail
                                                        // deferredCredentialMetadataService.updateByTransactionCode(
                                                                // transactionCode,
                                                                // preAuthorizedCodeResponse.grant().preAuthorizedCode(),
                                                                // preAuthorizedCodeResponse.txCode()
                                                                // credentialOwnerEmail,
                                                                // credentialOffer)
                                                        // --> DeferredCredentialMetadata
                                                        // buildCredentialOfferUri(deferredCredentialMetadata)
                                                            // Generate UUID
                                                            // Persiste UUID (key) and deferredCredentialMetadata (value)
                                                        // --> CredentialOfferUri
                                                        // todo: delete porque lo hace el generatePreAuthorizedCode()
                                                        deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(transactionCode, preAuthorizedCodeResponse.grant().preAuthorizedCode())
                                                                .then(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId))
                                                                .flatMap(email ->
                                                                        // todo: update DeferredCredentialMetadata.updateRecordWithCredentialOwnerEmail()
                                                                        // todo: buildCredentialOffer and update DeferredCredentialMetadata with credentialOffer
                                                                        // el buildCredentialOffer solo DEBE generar el objeto CustomCredentialOffer
                                                                        credentialOfferService.buildCustomCredentialOffer(
                                                                                        credentialProcedure.getCredentialType(),
                                                                                        preAuthorizedCodeResponse.grant(),
                                                                                        email,
                                                                                        preAuthorizedCodeResponse.pin()
                                                                                )
//                                                                              // todo: delete saveCustomCredentialOffer
                                                                                .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                                                                                // el createCredentialOfferUriResponse debe ser buildCredentialOfferUri el cual
                                                                                // acepta el objeto CredentialOffer como parametro de función, lo persiste en caché y
                                                                                // genera el CredentialOfferUri
                                                                                // credentialOfferService.buildCredentialOfferUri(deferredCredentialMetadata)
                                                                                .flatMap(credentialOfferService::createCredentialOfferUriResponse)
                                                                )
                                                )
                                                .flatMap(credentialOfferUri ->
                                                        // esta función actualiza el transactionCode (activation code) con el valor del c_transaction_cide
                                                        // en DeferredCredentialMetadata
                                                        // Crea un c_transaction_code y lo vincula el transaction_code original
                                                        // Este c_transaction_code es devuelto con el CredentialOfferUriResponse
                                                        deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(transactionCode)
                                                                .map(cTransactionCodeMap ->
                                                                                CredentialOfferUriResponse.builder()
                                                                                        .credentialOfferUri(credentialOfferUri)
                                                                                        .cTransactionCode(cTransactionCodeMap.get("cTransactionCode").toString())
                                                                                        .cTransactionCodeExpiresIn(Integer.parseInt(cTransactionCodeMap.get("cTransactionCodeExpiresIn").toString()))
                                                                                        .build()
                                                                )
                                                )
                                )
                );
    }


    @Override
    public Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce) {
        // todo: CredentialOfferService.getCredentialOfferById(id)
        //  CredentialOfferCacheStorageRepository.findById(id)
        //  --> DeferredCredentialMetadata
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce)
                .flatMap(credentialOfferData ->
                        // deferredCredentialMetadata.credentialOwnerEmail, deferredCredentialMetadata.txCode
                        emailService.sendPin(credentialOfferData.employeeEmail(), "Pin Code", credentialOfferData.pin())
                                // deferredCredentialMetadata.credentialOffer
                        .then(Mono.just(credentialOfferData.credentialOffer()))
                );
    }

}
