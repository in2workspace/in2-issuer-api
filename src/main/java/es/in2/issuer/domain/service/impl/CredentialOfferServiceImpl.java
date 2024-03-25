package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Base64URL;
import es.in2.issuer.domain.exception.CredentialTypeUnsuportedException;
import es.in2.issuer.domain.exception.ExpiredPreAuthorizedCodeException;
import es.in2.issuer.domain.model.CredentialsSupported;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.model.VcTemplate;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import es.in2.issuer.infrastructure.repository.CacheStore;
//import eu.europa.ec.eudi.openid4vci.CredentialOffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;
//import static es.in2.issuer.domain.util.Utils.generateNonce;
//import static io.ktor.client.statement.HttpResponseKt.getRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialOfferServiceImpl implements CredentialOfferService {

    private final AppConfiguration appConfiguration;

//    public Mono<CredentialOffer> buildCredentialOffer() {
//        return null;
//    }

    @Override
    public Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType) {
        return Mono.just(CustomCredentialOffer.builder()
                // todo: revisar getIssuerDomain si es necesario getIssuerExternalDomain (https://domain.org)
                .credentialIssuer(appConfiguration.getIssuerDomain())
                .credentials(List.of(
                        new CustomCredentialOffer.Credential(JWT_VC, List.of(credentialType)),
                        new CustomCredentialOffer.Credential(CWT_VC, List.of(credentialType))
                ))
                .grants(Map.of(GRANT_TYPE, new Grant(PRE_AUTHORIZATION_CODE, true)))
                .build());
    }

    @Override
    public Mono<String> createCredentialOfferUri(String nonce) {
        return Mono.just(
                ensureUrlHasProtocol(appConfiguration.getIssuerDomain() + "/credential-offer?credential_offer_uri=") +
                        ensureUrlHasProtocol(appConfiguration.getIssuerDomain() + "/credential-offer/" + nonce)
        );
    }
//
//    public Mono<String> generateCredentialOffer(String accessToken, String credentialType) {
//        String credentialTypeAux = (credentialType != null) ? credentialType : LEAR_CREDENTIAL;
//        return checkCredentialInCredentialsSupported(credentialTypeAux)
//                .then(generateCredentialOfferAux(accessToken, credentialTypeAux))
//                .flatMap(this::storeCredentialOfferInMemoryCache);
//    }
//
//    private Mono<CustomCredentialOffer> generateCredentialOfferAux(String accessToken, String credentialType) {
//        return getPreAuthorizationCodeFromKeycloak(accessToken)
//                .map(preAuthorizedCode -> CustomCredentialOffer.builder()
//                        .credentialIssuer(ensureUrlHasProtocol(credentialIssuerExternalDomain))
//                        .credentials(
//                                Arrays.asList(
//                                        new CustomCredentialOffer.Credential("jwt_vc", Collections.singletonList(credentialType)),
//                                        new CustomCredentialOffer.Credential("cwt_vc", Collections.singletonList(credentialType))
//                                )
//                        )
//                        .grants(Collections.singletonMap(GRANT_TYPE, new Grant(preAuthorizedCode, false)))
//                        .build());
//    }
//
//    private Mono<Void> checkCredentialInCredentialsSupported(String credentialType) {
//        return credentialIssuerMetadataService.generateOpenIdCredentialIssuer()
//                .flatMap(credentialIssuerMetadata -> {
//                    for (CredentialsSupported credential : credentialIssuerMetadata.credentialsSupported()) {
//                        VcTemplate vcTemplate = credential.credentialSubject();
//                        if (vcTemplate.name().equals(credentialType)) {
//                            return Mono.empty();
//                        }
//                    }
//                    return Mono.error(new CredentialTypeUnsuportedException("Credential Type '" + credentialType + "' not in credentials supported"));
//                });
//    }
//
//
//
//    private Mono<String> getPreAuthorizationCodeFromKeycloak(String accessToken) {
//        String preAuthCodeUri = iamAdapterFactory.getAdapter().getPreAuthCodeUri();
//        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";
//        return Mono.fromCallable(() -> executeGetRequest(url, accessToken))
//                .flatMap(responseMono -> responseMono.flatMap(response -> {
//                    try {
//                        JsonNode jsonObject = objectMapper.readTree(response);
//                        return Mono.just(jsonObject.path("grants").path("pre-authorized_code").asText());
//                    } catch (JsonProcessingException e) {
//                        return Mono.error(new RuntimeException("Error parsing JSON response", e));
//                    }
//                }));
//    }
//
//    private Mono<String> executeGetRequest(String url, String token) {
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
//        return getRequest(url, headers);
//    }
//
//    /**
//     * Store the Credential Offer in cache and return the nonce code to be used in the Credential Offer URI
//     */
//    private Mono<String> storeCredentialOfferInMemoryCache(CustomCredentialOffer credentialOffer) {
//        return generateNonce()
//                .flatMap(nonce -> {
//                    log.debug("***** Nonce code: " + nonce);
//                    cacheStore.add(nonce, credentialOffer);
//                    return Mono.just(nonce);
//                });
//    }
//
//    @Override
//    public Mono<CustomCredentialOffer> getCredentialOffer(String id) {
//        return checkIfCacheExistsById(id)
//                .doOnSuccess(credentialOffer -> cacheStore.delete(id));
//    }
//
//    private Mono<CustomCredentialOffer> checkIfCacheExistsById(String id) {
//        return Mono.defer(() -> {
//            CustomCredentialOffer credentialOffer = cacheStore.get(id);
//            if (credentialOffer != null) {
//                return Mono.just(credentialOffer);
//            } else {
//                return Mono.error(new ExpiredPreAuthorizedCodeException("pre-authorized_code is expired or used"));
//            }
//        });
//    }

}
