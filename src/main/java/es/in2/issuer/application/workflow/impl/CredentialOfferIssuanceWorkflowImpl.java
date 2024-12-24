package es.in2.issuer.application.workflow.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.domain.exception.CredentialAlreadyIssuedException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.exception.PreAuthorizationCodeGetException;
import es.in2.issuer.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.domain.model.dto.PreAuthCodeResponse;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.Constants.BEARER_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceWorkflowImpl implements CredentialOfferIssuanceWorkflow {

    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    private final ObjectMapper objectMapper;
    private final WebClientConfig webClient;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final AuthServerConfig authServerConfig;
    private final IssuerApiClientTokenService issuerApiClientTokenService;

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

    private Mono<CredentialOfferUriResponse> buildCredentialOfferUriInternal(String transactionCode) {
        return deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                .flatMap(procedureId ->
                        // Validate if the credential has already been issued
                        credentialProcedureService.getCredentialStatusByProcedureId(procedureId)
                                .flatMap(credentialStatusString -> {
                                    CredentialStatus credentialStatus;
                                    try {
                                        credentialStatus = CredentialStatus.valueOf(credentialStatusString);
                                    } catch (IllegalArgumentException | NullPointerException e) {
                                        return Mono.error(new IllegalStateException("Non valid status: " + credentialStatusString));
                                    }

                                    if (credentialStatus == CredentialStatus.DRAFT || credentialStatus == CredentialStatus.WITHDRAWN) {
                                        return Mono.just(procedureId);
                                    } else {
                                        return Mono.error(new CredentialAlreadyIssuedException("The credential has already been issued"));
                                    }
                                })
                                .flatMap(validProcedureId ->
                                        credentialProcedureService.getCredentialTypeByProcedureId(validProcedureId)
                                                .flatMap(credentialType ->
                                                        getPreAuthorizationCodeFromIam()
                                                                .flatMap(preAuthCodeResponse ->
                                                                        deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(
                                                                                        transactionCode,
                                                                                        preAuthCodeResponse.grant().preAuthorizedCode()
                                                                                )
                                                                                .then(
                                                                                        credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(validProcedureId)
                                                                                )
                                                                                .flatMap(email ->
                                                                                        credentialOfferService.buildCustomCredentialOffer(
                                                                                                        credentialType,
                                                                                                        preAuthCodeResponse.grant(),
                                                                                                        email,
                                                                                                        preAuthCodeResponse.pin()
                                                                                                )
                                                                                                .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                                                                                                .flatMap(credentialOfferService::createCredentialOfferUriResponse)
                                                                                )
                                                                )
                                                                .flatMap(credentialOfferUri ->
                                                                        deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(transactionCode)
                                                                                .flatMap(cTransactionCode ->
                                                                                        Mono.just(
                                                                                                CredentialOfferUriResponse.builder()
                                                                                                        .credentialOfferUri(credentialOfferUri)
                                                                                                        .cTransactionCode(cTransactionCode)
                                                                                                        .build()
                                                                                        )
                                                                                )
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

    private Mono<PreAuthCodeResponse> getPreAuthorizationCodeFromIam() {
        String preAuthCodeUri = authServerConfig.getPreAuthCodeUri();
        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";

        // Get request
        return issuerApiClientTokenService.getClientToken()
                .flatMap(
                        token ->
                            webClient.commonWebClient()
                            .get()
                            .uri(url)
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                            .exchangeToMono(response -> {
                                if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                                    return Mono.error(new PreAuthorizationCodeGetException("There was an error during pre-authorization code retrieval, error: " + response));
                                } else {
                                    log.info("Pre Authorization code response: {}", response);
                                    return response.bodyToMono(String.class);
                                }
                            })
                            // Parsing response
                            .flatMap(response -> {
                                try {
                                    return Mono.just(objectMapper.readValue(response, PreAuthCodeResponse.class));
                                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                    return Mono.error(new ParseErrorException("Error parsing JSON response"));
                                }
                            })
                );
    }

}
