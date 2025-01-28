package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.SignedDataParsingException;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.SignatureType;
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

    private final String credentialID = "SECRETO";
    private final String credentialPassword = "SECRETO";
    private final String clientId = "SECRETO";
    private final String clientSecret = "SECRETO";


    @Override
    public Mono<SignedData> sign(SignatureRequest signatureRequest, String token) {
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
        if (remoteSignatureConfig.getRemoteSignatureDomain().contains("dss")) {
            return getSignedDocumentDSS(signatureRequest, token);
        } else {
            return getSignedDocumentExternal(signatureRequest);
        }
    }

    private Mono<String> getSignedDocumentDSS(SignatureRequest signatureRequest, String token) {
        String signatureRemoteServerEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/api/v1"
                + remoteSignatureConfig.getRemoteSignatureSignPath();
        String signatureRequestJSON;
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

    public Mono<String> getSignedDocumentExternal(SignatureRequest signatureRequest){
        String grantType = "client_credentials";
        String scope = "credential";
        String signatureQualifier = "eu_eidas_qes";
        String signatureFormat = "J";
        String conformanceLevel = "Ades-B-B";
        String signAlgorithm = "OID_sign_algorithm";
        String hashAlgorithmOID = "2.16.840.1.101.3.4.2.1";
        String type = "credential";
        String credentialType = SignatureType.JADES.name();

        String requestBodyString;
        String signatureRemoteServerEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/csc/v2/signatures/signDoc";
        String signatureGetAccessTokenEndpoint = remoteSignatureConfig.getRemoteSignatureDomain() + "/oauth2/token";

        Map<String, String> requestBodyToAccess = new HashMap<>();
        requestBodyToAccess.put("grant_type", grantType);
        requestBodyToAccess.put("scope", scope);
        requestBodyToAccess.put("authorization_details", buildAuthorizationDetails(signatureRequest.data(), hashAlgorithmOID, type));

        try {
            requestBodyString = objectMapper.writeValueAsString(requestBodyToAccess);
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }

        List<Map.Entry<String, String>> headersAccess = new ArrayList<>();

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String basicAuthHeader = "Basic " + encodedAuth;

        headersAccess.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, basicAuthHeader));
        headersAccess.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        return httpUtils.postRequest(signatureGetAccessTokenEndpoint, headersAccess, requestBodyString)
            .flatMap(responseJson -> Mono.fromCallable(() -> {
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
                    return (String) responseMap.get("access_token");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error parsing access token response", e);
                }
            }))
            .flatMap(accessToken -> {
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
                //CONSTRUIR LA ESTRUCTURA QUE DEVUELVE EL DSS
            });
    }

    private String buildAuthorizationDetails(String unsignedCredential, String hashAlgorithmOID, String type) {
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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error building authorization details", e);
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
