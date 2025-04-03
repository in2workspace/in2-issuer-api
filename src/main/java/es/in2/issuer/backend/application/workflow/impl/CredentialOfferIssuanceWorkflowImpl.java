package es.in2.issuer.backend.application.workflow.impl;

import es.in2.issuer.authserver.application.workflow.PreAuthorizedCodeWorkflow;
import es.in2.issuer.backend.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.backend.domain.service.*;
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
        return deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                .flatMap(procedureId ->
                        credentialProcedureService.getCredentialProcedureByProcedureId(procedureId)
                                .flatMap(credentialProcedure ->
                                        preAuthorizedCodeWorkflow.generatePreAuthorizedCodeResponse(Mono.just(credentialProcedure.getCredentialId()))
                                                .flatMap(preAuthorizedCodeResponse ->
                                                        deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(
                                                                        transactionCode,
                                                                        preAuthorizedCodeResponse.grant().preAuthorizedCode()
                                                                )
                                                                .then(
                                                                        credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId)
                                                                )
                                                                .flatMap(email ->
                                                                        credentialOfferService.buildCustomCredentialOffer(
                                                                                        credentialProcedure.getCredentialType(),
                                                                                        preAuthorizedCodeResponse.grant(),
                                                                                        email,
                                                                                        preAuthorizedCodeResponse.pin()
                                                                                )
                                                                                .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                                                                                .flatMap(credentialOfferService::createCredentialOfferUriResponse)
                                                                )
                                                )
                                                .flatMap(credentialOfferUri ->
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
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce)
                .flatMap(credentialOfferData -> emailService.sendPin(credentialOfferData.employeeEmail(), "Pin Code", credentialOfferData.pin())
                        .then(Mono.just(credentialOfferData.credentialOffer()))
                );
    }

}
