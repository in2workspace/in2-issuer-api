package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.domain.service.VcSchemaService;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import es.in2.issuer.domain.util.HttpUtils;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferIssuanceServiceImpl implements CredentialOfferIssuanceService {

    private final VcSchemaService vcSchemaService;
    private final CredentialOfferService credentialOfferService;
    private final CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    private final IamAdapterFactory iamAdapterFactory;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> buildCredentialOfferUri(String accessToken, String credentialType) {
        return vcSchemaService.isSupportedVcSchema(credentialType)
                .flatMap(isSupported -> {
                    if (!isSupported) {
                        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
                    }
                    // Use flatMap to chain the Mono from getPreAuthorizationCodeFromIam
                    return getPreAuthorizationCodeFromIam(accessToken)
                            .flatMap(preAuthorizationCode ->
                                    credentialOfferService.buildCustomCredentialOffer(credentialType, preAuthorizationCode)
                            )
                            .flatMap(credentialOfferCacheStorageService::saveCustomCredentialOffer)
                            .flatMap(credentialOfferService::createCredentialOfferUri);
                });
    }

    @Override
    public Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce) {
        return credentialOfferCacheStorageService.getCustomCredentialOffer(nonce);
    }

    private Mono<String> getPreAuthorizationCodeFromIam(String accessToken) {
        String preAuthCodeUri = iamAdapterFactory.getAdapter().getPreAuthCodeUri();
        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";

        // Adjusting the call to use the reactive prepareHeadersWithAuth
        return httpUtils.prepareHeadersWithAuth(accessToken)
                .flatMap(headers -> httpUtils.getRequest(url, headers))
                .flatMap(response -> {
                    try {
                        JsonNode jsonObject = objectMapper.readTree(response);
                        return Mono.just(jsonObject.path("grants").path("pre-authorized_code").asText());
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error parsing JSON response", e));
                    }
                });
    }
}
