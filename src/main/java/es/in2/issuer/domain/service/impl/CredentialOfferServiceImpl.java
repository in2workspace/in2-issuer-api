package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Base64URL;
import es.in2.issuer.domain.exception.CredentialTypeUnsuportedException;
import es.in2.issuer.domain.exception.ExpiredPreAuthorizedCodeException;
import es.in2.issuer.domain.model.CredentialOfferForPreAuthorizedCodeFlow;
import es.in2.issuer.domain.model.CredentialsSupported;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.domain.service.CredentialOfferService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import es.in2.issuer.infrastructure.repository.CacheStore;
import id.walt.credentials.w3c.templates.VcTemplate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.GRANT_TYPE;
import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialOfferServiceImpl implements CredentialOfferService {

    private final CacheStore<CredentialOfferForPreAuthorizedCodeFlow> cacheStore;
    private final CredentialIssuerMetadataService credentialIssuerMetadataService;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper;
    private final AppConfiguration appConfiguration;
    private final IamAdapterFactory iamAdapterFactory;

    // fixme: ¿Por qué hay un postconstruct aquí con el issuerAPIBaseUrl?
    private String issuerApiBaseUrl;
    @PostConstruct
    private void initializeProperties() {
        issuerApiBaseUrl = appConfiguration.getIssuerDomain();
    }

    @Override
    public Mono<String> createCredentialOfferUriForPreAuthorizedCodeFlow(String accessToken, String credentialType) {
        return generateCredentialOffer(accessToken, credentialType)
                .map(credentialOfferId -> {
                    String credentialOfferUri = HttpUtils.ensureUrlHasProtocol(issuerApiBaseUrl + "/credential-offer/" + credentialOfferId);
                    String credentialOfferPrefix = HttpUtils.ensureUrlHasProtocol(issuerApiBaseUrl + "/credential-offer?credential_offer_uri=");
                    return credentialOfferPrefix + credentialOfferUri;
                });
    }

    public Mono<String> generateCredentialOffer(String accessToken, String credentialType) {
        String credentialTypeAux = (credentialType != null) ? credentialType : LEAR_CREDENTIAL;
        return checkCredentialInCredentialsSupported(credentialTypeAux)
                .then(generateCredentialOfferAux(accessToken, credentialTypeAux))
                .flatMap(this::storeCredentialOfferInMemoryCache);
    }

    private Mono<CredentialOfferForPreAuthorizedCodeFlow> generateCredentialOfferAux(String accessToken, String credentialType) {
        return getPreAuthorizationCodeFromKeycloak(accessToken).map(preAuthorizedCode -> CredentialOfferForPreAuthorizedCodeFlow.builder().credentialIssuer(HttpUtils.ensureUrlHasProtocol(issuerApiBaseUrl)).credentials(Collections.singletonList(credentialType)).grants(Collections.singletonMap(GRANT_TYPE, new Grant(preAuthorizedCode, false))).build());
    }

    private Mono<Void> checkCredentialInCredentialsSupported(String credentialType) {
        return credentialIssuerMetadataService.generateOpenIdCredentialIssuer()
                .flatMap(credentialIssuerMetadata -> {
                    for (CredentialsSupported credential : credentialIssuerMetadata.credentialsSupported()) {
                        VcTemplate vcTemplate = credential.credentialSubject();
                        if (vcTemplate.getName().equals(credentialType)) {
                            return Mono.empty();
                        }
                    }
                    return Mono.error(new CredentialTypeUnsuportedException("Credential Type '" + credentialType + "' not in credentials supported"));
                });
    }

    private Mono<String> generateNonce() {
        return convertUUIDToBytes(UUID.randomUUID())
                .map(uuidBytes -> Base64URL.encode(uuidBytes).toString());
    }

    private Mono<String> getPreAuthorizationCodeFromKeycloak(String accessToken) {
        String preAuthCodeUri = iamAdapterFactory.getAdapter().getPreAuthCodeUri();
        String url = preAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json";
        return Mono.fromCallable(() -> executeGetRequest(url, accessToken))
                .flatMap(responseMono -> responseMono.flatMap(response -> {
                    try {
                        JsonNode jsonObject = objectMapper.readTree(response);
                        return Mono.just(jsonObject.path("grants").path("pre-authorized_code").asText());
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error parsing JSON response", e));
                    }
                }));
    }

    private Mono<String> executeGetRequest(String url, String token) {

        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        return httpUtils.getRequest(url,headers);
    }

    /**
     * Store the Credential Offer in cache and return the nonce code to be used in the Credential Offer URI
     */
    private Mono<String> storeCredentialOfferInMemoryCache(CredentialOfferForPreAuthorizedCodeFlow credentialOffer) {
        return generateNonce()
                .flatMap(nonce -> {
                    log.debug("***** Nonce code: " + nonce);
                    cacheStore.add(nonce, credentialOffer);
                    return Mono.just(nonce);
                });
    }

    @Override
    public Mono<CredentialOfferForPreAuthorizedCodeFlow> getCredentialOffer(String id) {
        return checkIfCacheExistsById(id)
                .doOnSuccess(credentialOffer -> cacheStore.delete(id));
    }

    private Mono<byte[]> convertUUIDToBytes(UUID uuid) {
        return Mono.fromSupplier(() -> {
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            return byteBuffer.array();
        });
    }

    private Mono<CredentialOfferForPreAuthorizedCodeFlow> checkIfCacheExistsById(String id) {
        return Mono.defer(() -> {
            CredentialOfferForPreAuthorizedCodeFlow credentialOffer = cacheStore.get(id);
            if (credentialOffer != null) {
                return Mono.just(credentialOffer);
            } else {
                return Mono.error(new ExpiredPreAuthorizedCodeException("pre-authorized_code is expired or used"));
            }
        });
    }

}
