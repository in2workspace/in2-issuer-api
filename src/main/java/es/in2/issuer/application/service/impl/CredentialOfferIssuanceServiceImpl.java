package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.exception.PreAuthorizationCodeGetException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.PreAuthCodeResponse;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceServiceImpl implements CredentialOfferIssuanceService {

    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    private final IamAdapterFactory iamAdapterFactory;
    private final ObjectMapper objectMapper;
    private final WebClientConfig webClient;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<String> buildCredentialOfferUri(String processId, String transactionCode) {
        return  deferredCredentialMetadataService.validateTransactionCode(transactionCode)
                .then(deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode))
                .flatMap(procedureId -> credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                .flatMap(credentialType -> getPreAuthorizationCodeFromIam()
                        .flatMap(preAuthCodeResponse ->
                                deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(transactionCode,preAuthCodeResponse.grant().preAuthorizedCode())
                                        .then(credentialOfferService.buildCustomCredentialOffer(credentialType, preAuthCodeResponse.grant())
                                                .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                                                .flatMap(credentialOfferService::createCredentialOfferUri)
                                                .flatMap(credentialOfferUri ->{
                                                // After creating the credential offer URI, send the PIN email
                                                    return credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId)
                                                            .flatMap(email -> emailService.sendPin(email, "pin code", preAuthCodeResponse.pin()))
                                                                .thenReturn(credentialOfferUri);
                                                }
                                                )
                                )
                        )
                )
                );
    }

    @Override
    public Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce) {
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce);
    }

    private Mono<PreAuthCodeResponse> getPreAuthorizationCodeFromIam() {
        String preAuthCodeUri = iamAdapterFactory.getAdapter().getPreAuthCodeUri();
        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";

        // Get request
        return webClient.centralizedWebClient()
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        return Mono.error(new PreAuthorizationCodeGetException("There was an error during pre-authorization code retrieval, error: " + response));
                    } else if (response.statusCode().is3xxRedirection()) {
                        return Mono.just(Objects.requireNonNull(response.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION)));
                    } else {
                        log.info("Authorization Response: {}", response);
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
                });
    }
}
