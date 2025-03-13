package es.in2.issuer.domain.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.domain.util.JwtUtils;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import es.in2.issuer.infrastructure.repository.DeferredCredentialMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static es.in2.issuer.domain.util.Constants.ASYNC;
import static es.in2.issuer.domain.util.Constants.BEARER_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteSignatureServiceImpl implements RemoteSignatureService {

    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;
    private final JwtUtils jwtUtils;
    private final RemoteSignatureConfig remoteSignatureConfig;
    private final HashGeneratorService hashGeneratorService;
    private static final String ACCESS_TOKEN_NAME = "access_token";
    private static final String CREDENTIAL = "credential";
    private final CredentialProcedureRepository credentialProcedureRepository;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;
    private final List<Map.Entry<String, String>> headers = new ArrayList<>();
    private final Map<String, Object> requestBody = new HashMap<>();
    private String credentialID;
    private String credentialPassword;
    private String clientId;
    private String clientSecret;

    @Override
    //TODO Cuando se implementen los "settings" del issuer, se debe pasar el clientId, secret, etc. como parámetros en lugar de var entorno
    public Mono<SignedData> sign(SignatureRequest signatureRequest, String token, String procedureId) {
        clientId = remoteSignatureConfig.getRemoteSignatureClientId();
        clientSecret = remoteSignatureConfig.getRemoteSignatureClientSecret();
        return Mono.defer(() -> executeSigningFlow(signatureRequest, token, procedureId)
            .doOnSuccess(result -> {
                    log.info("Successfully Signed");
                    log.info("Procedure with id: {}", procedureId);
                    log.info("at time: {}", new Date());
                    deferredCredentialMetadataService.deleteDeferredCredentialMetadataById(procedureId);
            })
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(5))
                    .jitter(0.5)
                    .filter(this::isRecoverableError)   // Retry only on recoverable errors
                    .doBeforeRetry(retrySignal -> {
                        long attempt = retrySignal.totalRetries() + 1;
                        log.info("Retrying signing process due to recoverable error (Attempt #{} of 3)", attempt);
                    }))
            .onErrorResume(throwable -> {
                log.error("Error after 3 retries, switching to ASYNC mode.");
                log.error("Error Time: {}", new Date());
                return handlePostRecoverError(throwable, procedureId)
                        .then(Mono.error(new RemoteSignatureException("Signature Failed, changed to ASYNC mode", throwable)));
            }));
    }

    public boolean isRecoverableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError();
        } else return throwable instanceof ConnectException || throwable instanceof TimeoutException;
    }

    public Mono<Boolean> validateCredentials() {
        log.info("Validating credentials");
        SignatureRequest signatureRequest = SignatureRequest.builder().build();
        return requestAccessToken(signatureRequest, "service")
                .flatMap(this::validateCertificate);
    }

    private Mono<SignedData> executeSigningFlow(SignatureRequest signatureRequest, String token, String procedureId) {
        log.info("Signing credential with procedure id: {}", procedureId);
        return getSignedSignature(signatureRequest, token)
            .flatMap(response -> {
                try {
                    return Mono.just(toSignedData(response));
                } catch (SignedDataParsingException ex) {
                    return Mono.error(new RemoteSignatureException("Error parsing signed data", ex));
                }
            })
            .doOnSuccess(result -> {
                try {
                    log.info("Credential signed!");
                } catch (Exception e) {
                    log.warn("Failed to delete deferred credential metadata for procedureId {}: {}", procedureId, e.getMessage());
                }
            });
    }

    public Mono<Boolean> validateCertificate(String accessToken) {
        credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        String credentialListEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/csc/v2/credentials/list";
        headers.clear();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        requestBody.clear();
        requestBody.put("credentialInfo", true);
        requestBody.put("certificates", "chain");
        requestBody.put("certInfo", true);
        requestBody.put("authInfo", true);
        requestBody.put("onlyValid", true);
        requestBody.put("lang", 0);
        requestBody.put("clientData", "string");
        try {
            ObjectMapper objectMapperIntern = new ObjectMapper();
            String requestBodyJson = objectMapperIntern.writeValueAsString(requestBody);
            return httpUtils.postRequest(credentialListEndpoint, headers, requestBodyJson)
                    .flatMap(responseJson -> {
                        try {
                            Map<String, List<String>> responseMap = objectMapperIntern.readValue(responseJson, Map.class);
                            List<String> receivedCredentialIDs = responseMap.get("credentialIDs");
                            boolean isValid = receivedCredentialIDs != null &&
                                    receivedCredentialIDs.stream()
                                            .anyMatch(id -> id.trim().equalsIgnoreCase(credentialID.trim()));
                            return Mono.just(isValid);
                        } catch (JsonProcessingException e) {
                            return Mono.error(new RemoteSignatureException("Error parsing certificate list response", e));
                        }
                    })
                    .switchIfEmpty(Mono.just(false))
                    .doOnError(error -> log.error("Error validating certificate: {}", error.getMessage()));
        } catch (JsonProcessingException e) {
            return Mono.error(new RemoteSignatureException("Error serializing request body to JSON", e));
        }
    }

    public Mono<String> getSignedSignature(SignatureRequest signatureRequest, String token) {
        return switch (remoteSignatureConfig.getRemoteSignatureType()) {
            case "server" -> getSignedDocumentDSS(signatureRequest, token);
            case "cloud" -> getSignedDocumentExternal(signatureRequest);
            default -> Mono.error(new RemoteSignatureException("Remote signature service not available"));
        };
    }

    private Mono<String> getSignedDocumentDSS(SignatureRequest signatureRequest, String token) {
        String signatureRemoteServerEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/api/v1"
                + remoteSignatureConfig.getRemoteSignatureSignPath();
        String signatureRequestJSON;

        log.info("Requesting signature to DSS service");

        try {
            signatureRequestJSON = objectMapper.writeValueAsString(signatureRequest);
        } catch (JsonProcessingException e) {
            return Mono.error(new RemoteSignatureException("Error serializing signature request", e));
        }
        headers.clear();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        return httpUtils.postRequest(signatureRemoteServerEndpoint, headers, signatureRequestJSON)
                .doOnError(error -> log.error("Error signing credential with server method: {}", error.getMessage()));
    }

    public Mono<String> getSignedDocumentExternal(SignatureRequest signatureRequest) {
        log.info("Requesting signature to external service");
        return requestAccessToken(signatureRequest, CREDENTIAL)
                .flatMap(accessToken -> sendSignatureRequest(signatureRequest, accessToken))
                .flatMap(responseJson -> processSignatureResponse(signatureRequest, responseJson));
    }

    private Mono<String> requestAccessToken(SignatureRequest signatureRequest, String scope) {
        credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        credentialPassword = remoteSignatureConfig.getRemoteSignatureCredentialPassword();
        clientId = remoteSignatureConfig.getRemoteSignatureClientId();
        clientSecret = remoteSignatureConfig.getRemoteSignatureClientSecret();
        String grantType = "client_credentials";
        String signatureGetAccessTokenEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/oauth2/token";
        String hashAlgorithmOID = "2.16.840.1.101.3.4.2.1";

        requestBody.clear();
        requestBody.put("grant_type", grantType);
        requestBody.put("scope", scope);
        if(scope.equals(CREDENTIAL)){
            requestBody.put("authorization_details", buildAuthorizationDetails(signatureRequest.data(), hashAlgorithmOID));
        }

        String requestBodyString = requestBody.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        String basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        headers.clear();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, basicAuthHeader));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
        return httpUtils.postRequest(signatureGetAccessTokenEndpoint, headers, requestBodyString)
            .flatMap(responseJson -> Mono.fromCallable(() -> {
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
                    if (!responseMap.containsKey(ACCESS_TOKEN_NAME)) {
                        throw new AccessTokenException("Access token missing in response");
                    }
                    return (String) responseMap.get(ACCESS_TOKEN_NAME);
                } catch (JsonProcessingException e) {
                    throw new AccessTokenException("Error parsing access token response", e);
                }
            }))
            .onErrorResume(WebClientResponseException.class, ex ->{
                if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED){
                    return Mono.error(new RemoteSignatureException("Unauthorized: Invalid credentials"));
                }
                return Mono.error(ex);
            })
            .doOnError(error -> log.error("Error retrieving access token: {}", error.getMessage()));
    }

    private Mono<String> sendSignatureRequest(SignatureRequest signatureRequest, String accessToken) {
        credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        String signatureRemoteServerEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/csc/v2/signatures/signDoc";
        String signatureQualifier = "eu_eidas_qes";
        String signatureFormat = "J";
        String conformanceLevel = "Ades-B-B";
        String signAlgorithm = "OID_sign_algorithm";

        String base64Document = Base64.getEncoder().encodeToString(signatureRequest.data().getBytes(StandardCharsets.UTF_8));
        requestBody.clear();
        requestBody.put("credentialID", credentialID);
        requestBody.put("signatureQualifier", signatureQualifier);
        List<Map<String, String>> documents = List.of(
                Map.of(
                        "document", base64Document,
                        "signature_format", signatureFormat,
                        "conformance_level", conformanceLevel,
                        "signAlgo", signAlgorithm
                )
        );
        requestBody.put("documents", documents);

        String requestBodySignature;
        try {
            requestBodySignature = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing signature request", e));
        }

        headers.clear();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        return httpUtils.postRequest(signatureRemoteServerEndpoint, headers, requestBodySignature)
                .doOnError(error -> log.error("Error sending credential to sign: {}", error.getMessage()));
    }

    public Mono<String> processSignatureResponse(SignatureRequest signatureRequest, String responseJson) {
        return Mono.fromCallable(() -> {
            try {
                Map<String, List<String>> responseMap = objectMapper.readValue(responseJson, Map.class);
                List<String> documentsWithSignatureList = responseMap.get("DocumentWithSignature");

                if (documentsWithSignatureList == null || documentsWithSignatureList.isEmpty()) {
                    throw new SignatureProcessingException("No signature found in the response");
                }
                String documentsWithSignature = documentsWithSignatureList.get(0);
                String documentsWithSignatureDecoded = new String(Base64.getDecoder().decode(documentsWithSignature), StandardCharsets.UTF_8);
                String receivedPayloadDecoded = jwtUtils.decodePayload(documentsWithSignatureDecoded);
                if(jwtUtils.areJsonsEqual(receivedPayloadDecoded, signatureRequest.data())){
                    return objectMapper.writeValueAsString(Map.of(
                            "type", signatureRequest.configuration().type().name(),
                            "data", documentsWithSignatureDecoded
                    ));
                } else {
                    throw new SignatureProcessingException("Signed payload received does not match the original data");
                }
            } catch (JsonProcessingException e) {
                throw new SignatureProcessingException("Error parsing signature response", e);
            }
        });
    }

    private String buildAuthorizationDetails(String unsignedCredential, String hashAlgorithmOID) {
        credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        credentialPassword = remoteSignatureConfig.getRemoteSignatureCredentialPassword();
        try {
            Map<String, Object> authorizationDetails = new HashMap<>();
            authorizationDetails.put("type", CREDENTIAL);
            authorizationDetails.put("credentialID", credentialID);
            authorizationDetails.put("credentialPassword", credentialPassword);
            String hashedCredential = hashGeneratorService.generateHash(unsignedCredential, hashAlgorithmOID);
            List<Map<String, String>> documentDigests = null;
            if (hashedCredential != null) {
                documentDigests = List.of(
                        Map.of("hash", hashedCredential, "label", "Issued Credential")
                );
            }
            authorizationDetails.put("documentDigests", documentDigests);
            authorizationDetails.put("hashAlgorithmOID", hashAlgorithmOID);

            return objectMapper.writeValueAsString(List.of(authorizationDetails));
        } catch (JsonProcessingException | HashGenerationException e) {
            throw new AuthorizationDetailsException("Error generating authorization details", e);
        }
    }

    private SignedData toSignedData(String signedSignatureResponse) throws SignedDataParsingException {
        try {
            return objectMapper.readValue(signedSignatureResponse, SignedData.class);
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            throw new SignedDataParsingException("Error parsing signed data");
        }
    }

    public Mono<String>handlePostRecoverError(Throwable error, String procedureId) {
        Mono<Void> updateOperationMode = credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
            .flatMap(credentialProcedure -> {
                credentialProcedure.setOperationMode(ASYNC);
                credentialProcedure.setCredentialStatus(CredentialStatus.PEND_SIGNATURE);
                return credentialProcedureRepository.save(credentialProcedure)
                        .doOnSuccess(result -> log.info("Updated operationMode to Async - Procedure"))
                        .then();
            });
        Mono<Void> updateDeferredMetadata = deferredCredentialMetadataRepository.findByProcedureId(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setOperationMode(ASYNC);
                    return deferredCredentialMetadataRepository.save(credentialProcedure)
                            .doOnSuccess(result -> log.info("Updated operationMode to Async - Deferred"))
                            .then();
                });

        return updateOperationMode
                .then(updateDeferredMetadata)
                .then(Mono.error(new RemoteSignatureException("Signature Failed, changed to ASYNC mode", error)));
    }
}