package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.service.HashGeneratorService;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.BEARER_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoteSignatureServiceImpl implements RemoteSignatureService {

    private final ObjectMapper objectMapper;
    private final HttpUtils httpUtils;
    private final RemoteSignatureConfig remoteSignatureConfig;
    private final HashGeneratorService hashGeneratorService;
    private static final String ACCESS_TOKEN_NAME = "access_token";

    @Override
    public Mono<SignedData> sign(SignatureRequest signatureRequest, String token) {
        log.info("Signing signature...");
        return getSignedSignature(signatureRequest, token)
                .flatMap(response -> {
                    try {
                        return Mono.just(toSignedData(response));
                    } catch (SignedDataParsingException ex) {
                        return Mono.error(ex);
                    }
                })
                .doOnSuccess(result -> log.info("Signature signed!"))
                .doOnError(throwable -> {
                    if (throwable instanceof SignedDataParsingException) {
                        log.error("Error parsing signed data: {}", throwable.getMessage());
                    } else {
                        log.error("Error: {}", throwable.getMessage());
                    }
                });
    }

    private Mono<String> getSignedSignature(SignatureRequest signatureRequest, String token) {
        log.info("externalService: {}", remoteSignatureConfig.getRemoteSignatureExternalService());
        return switch (remoteSignatureConfig.getRemoteSignatureExternalService()) {
            case "false" -> getSignedDocumentDSS(signatureRequest, token);
            case "true" -> getSignedDocumentExternal(signatureRequest);
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
            return Mono.error(e);
        }
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        // todo: refactorizar: debe implementarse la llamada aquí y no en el Utils
        return httpUtils.postRequest(signatureRemoteServerEndpoint, headers, signatureRequestJSON);
    }

    public Mono<String> getSignedDocumentExternal(SignatureRequest signatureRequest) {
        String hashAlgorithmOID = "2.16.840.1.101.3.4.2.1";
        String type = "credential";

        log.info("External signature service");

        return requestAccessToken(signatureRequest, hashAlgorithmOID, type)
                .flatMap(accessToken -> sendSignatureRequest(signatureRequest, accessToken))
                .flatMap(responseJson -> processSignatureResponse(signatureRequest, responseJson));
    }

    private Mono<String> requestAccessToken(SignatureRequest signatureRequest, String hashAlgorithmOID, String type) {
        String clientId = remoteSignatureConfig.getRemoteSignatureClientId();
        String clientSecret = remoteSignatureConfig.getRemoteSignatureClientSecret();
        String grantType = "client_credentials";
        String scope = "credential";
        String signatureGetAccessTokenEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/oauth2/token";

        log.info("Requesting access token");
        log.info("ClientId is: {}", clientId);
        log.info("ClientSecret is: {}", clientSecret);

        Map<String, String> requestBodyToAccess = new HashMap<>();
        requestBodyToAccess.put("grant_type", grantType);
        requestBodyToAccess.put("scope", scope);
        requestBodyToAccess.put("authorization_details", buildAuthorizationDetails(signatureRequest.data(), hashAlgorithmOID, type));

        String requestBodyString = requestBodyToAccess.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");

        List<Map.Entry<String, String>> headersAccess = new ArrayList<>();
        String basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        headersAccess.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, basicAuthHeader));
        headersAccess.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        return httpUtils.postRequest(signatureGetAccessTokenEndpoint, headersAccess, requestBodyString)
                .flatMap(responseJson -> Mono.fromCallable(() -> {
                    try {
                        Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
                        if (!responseMap.containsKey(ACCESS_TOKEN_NAME)) {
                            throw new AccessTokenException("Access token missing in response");
                        }
                        log.info("External Access token response: {}", responseMap.get(ACCESS_TOKEN_NAME));
                        return (String) responseMap.get(ACCESS_TOKEN_NAME);
                    } catch (JsonProcessingException e) {
                        throw new AccessTokenException("Error parsing access token response", e);
                    }
                }));
    }

    private Mono<String> sendSignatureRequest(SignatureRequest signatureRequest, String accessToken) {
        String credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        String signatureRemoteServerEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/csc/v2/signatures/signDoc";
        String signatureQualifier = "eu_eidas_qes";
        String signatureFormat = "J";
        String conformanceLevel = "Ades-B-B";
        String signAlgorithm = "OID_sign_algorithm";

        String base64Document = Base64.getEncoder().encodeToString(signatureRequest.data().getBytes(StandardCharsets.UTF_8));

        Map<String, Object> requestBodyToSign = new HashMap<>();
        requestBodyToSign.put("credentialID", credentialID);
        requestBodyToSign.put("signatureQualifier", signatureQualifier);
        List<Map<String, String>> documents = List.of(
                Map.of(
                        "document", base64Document,
                        "signature_format", signatureFormat,
                        "conformance_level", conformanceLevel,
                        "signAlgo", signAlgorithm
                )
        );
        requestBodyToSign.put("documents", documents);

        String requestBodySignature;
        try {
            requestBodySignature = objectMapper.writeValueAsString(requestBodyToSign);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing signature request", e));
        }

        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        return httpUtils.postRequest(signatureRemoteServerEndpoint, headers, requestBodySignature);
    }

    public Mono<String> processSignatureResponse(SignatureRequest signatureRequest, String responseJson) {
        log.info("Processing signature response: {}", responseJson);
        return Mono.fromCallable(() -> {
            try {
                Map<String, List<String>> responseMap = objectMapper.readValue(responseJson, Map.class);
                List<String> documentsWithSignatureList = responseMap.get("DocumentWithSignature");

                if (documentsWithSignatureList == null || documentsWithSignatureList.isEmpty()) {
                    throw new SignatureProcessingException("No signature found in the response");
                }

                String documentsWithSignature = documentsWithSignatureList.get(0);
                String documentsWithSignatureDecoded = new String(Base64.getDecoder().decode(documentsWithSignature), StandardCharsets.UTF_8);

                return objectMapper.writeValueAsString(Map.of(
                        "type", signatureRequest.configuration().type().name(),
                        "data", documentsWithSignatureDecoded
                ));

            } catch (JsonProcessingException e) {
                throw new SignatureProcessingException("Error parsing signature response", e);
            }
        });
    }

    private String buildAuthorizationDetails(String unsignedCredential, String hashAlgorithmOID, String type) {
        String credentialID = remoteSignatureConfig.getRemoteSignatureCredentialId();
        String credentialPassword = remoteSignatureConfig.getRemoteSignatureCredentialPassword();
        try {
            Map<String, Object> authorizationDetails = new HashMap<>();
            authorizationDetails.put("type", type);
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
}
