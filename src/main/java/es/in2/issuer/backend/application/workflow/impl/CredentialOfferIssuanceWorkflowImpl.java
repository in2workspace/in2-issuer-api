package es.in2.issuer.backend.application.workflow.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.backend.domain.exception.ParseErrorException;
import es.in2.issuer.backend.domain.exception.PreAuthorizationCodeGetException;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.backend.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.backend.domain.service.*;
import es.in2.issuer.backend.infrastructure.config.AuthServerConfig;
import es.in2.issuer.backend.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.backend.domain.util.Constants.BEARER_PREFIX;

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

    // Add logs to debug the process
    private Mono<CredentialOfferUriResponse> buildCredentialOfferUriInternal(String transactionCode) {
        return deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                .flatMap(procedureId ->
                        credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                                .flatMap(credentialType ->
                                        getPreAuthorizationCodeFromIam()
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
                                                                                        credentialType,
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

    private Mono<PreAuthorizedCodeResponse> getPreAuthorizationCodeFromIam() {
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
                                                return Mono.just(objectMapper.readValue(response, PreAuthorizedCodeResponse.class));
                                            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                                                return Mono.error(new ParseErrorException("Error parsing JSON response"));
                                            }
                                        })
                );
    }

}
