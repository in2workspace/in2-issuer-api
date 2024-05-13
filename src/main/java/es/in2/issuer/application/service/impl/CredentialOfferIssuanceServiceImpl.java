package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.exception.PreAuthorizationCodeGetException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.domain.service.VcSchemaService;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static es.in2.issuer.domain.util.Constants.BEARER;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceServiceImpl implements CredentialOfferIssuanceService {

    private final VcSchemaService vcSchemaService;
    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    private final IamAdapterFactory iamAdapterFactory;
    private final ObjectMapper objectMapper;
    private final WebClientConfig webClient;

    @Override
    public Mono<String> buildCredentialOfferUri(String accessToken, String credentialType) {
        return vcSchemaService.isSupportedVcSchema(credentialType)
                .flatMap(isSupported -> {
                    if (Boolean.FALSE.equals(isSupported)) {
                        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
                    }
                    // Use flatMap to chain the Mono from getPreAuthorizationCodeFromIam
                    return getPreAuthorizationCodeFromIam(accessToken)
                            .flatMap(grant ->
                                    credentialOfferService.buildCustomCredentialOffer(credentialType, grant)
                            )
                            .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                            .flatMap(credentialOfferService::createCredentialOfferUri);
                });
    }

    @Override
    public Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce) {
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce);
    }
    private Mono<Grant> getPreAuthorizationCodeFromIam(String accessToken) {
        String preAuthCodeUri = iamAdapterFactory.getAdapter().getPreAuthCodeUri();
        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";

        // Get request
        return webClient.centralizedWebClient()
                .get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, BEARER + accessToken)
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
                        return Mono.just(objectMapper.readValue(response, Grant.class));
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        return Mono.error(new ParseErrorException("Error parsing JSON response"));
                    }
                });
    }
}
