package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.AccessTokenException;
import es.in2.issuer.domain.exception.HashGenerationException;
import es.in2.issuer.domain.exception.SignatureProcessingException;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteSignatureServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpUtils httpUtils;

    @Mock
    private RemoteSignatureConfig remoteSignatureConfig;

    @InjectMocks
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Mock
    private HashGeneratorService hashGeneratorService;

    private SignatureRequest signatureRequest;
    private String token;
    private String signatureRemoteServerEndpoint;
    private SignatureType signatureType;

    @Test
    void testSignSuccessDSS() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("server");
        signatureType = SignatureType.COSE;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration1 = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration1, "data");
        token = "dummyToken";
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature-dss.com");
        when(remoteSignatureConfig.getRemoteSignatureSignPath()).thenReturn("/sign");
        signatureRemoteServerEndpoint = "http://remote-signature-dss.com/api/v1/sign";

        String signatureRequestJSON = "{\"request\":\"data\"}";
        String signedResponse = "{\"signed\":\"data\"}";
        String data = "data";
        SignedData signedData = new SignedData(signatureType, data);

        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn(signatureRequestJSON);
        when(httpUtils.postRequest(eq(signatureRemoteServerEndpoint), any(), eq(signatureRequestJSON)))
                .thenReturn(Mono.just(signedResponse));
        when(objectMapper.readValue(signedResponse, SignedData.class)).thenReturn(signedData);

        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token);

        StepVerifier.create(result)
                .expectNext(signedData)
                .verifyComplete();
    }

    @Test
    void testSignJsonProcessingException() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("server");
        signatureType = SignatureType.COSE;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration1 = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration1, "data");
        token = "dummyToken";
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature-dss.com");
        when(remoteSignatureConfig.getRemoteSignatureSignPath()).thenReturn("/sign");

        when(objectMapper.writeValueAsString(any(SignatureRequest.class)))
                .thenThrow(new JsonProcessingException("error") {});


        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token);

        StepVerifier.create(result)
                .expectError(JsonProcessingException.class)
                .verify();
    }

    @Test
    void testGetSignedDocumentExternal() throws JsonProcessingException, HashGenerationException {
        signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration, "data");

        String accessTokenResponse = "{\"access_token\": \"mock-access-token\"}";
        String base64SignedDocument = Base64.getEncoder().encodeToString("mock-signed-document".getBytes(StandardCharsets.UTF_8));
        String signedDocumentResponse = "{ \"DocumentWithSignature\": [\"" + base64SignedDocument + "\"] }";
        String processedResponse = "{ \"type\": \"JADES\", \"data\": \"mock-signed-document\" }";

        String hashAlgo = "2.16.840.1.101.3.4.2.1";

        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://api.external.com");

        doReturn(Mono.just(accessTokenResponse))
                .when(httpUtils)
                .postRequest(eq("https://api.external.com/oauth2/token"), any(List.class), anyString());

        doReturn(Mono.just(signedDocumentResponse))
                .when(httpUtils)
                .postRequest(eq("https://api.external.com/csc/v2/signatures/signDoc"), any(List.class), anyString());

        when(objectMapper.writeValueAsString(any())).thenReturn("mock-json");

        when(objectMapper.readValue(accessTokenResponse, Map.class))
                .thenReturn(Map.of("access_token", "mock-access-token"));

        when(hashGeneratorService.generateHash(signatureRequest.data(), hashAlgo)).thenReturn("mock-hash");

        when(objectMapper.readValue(signedDocumentResponse, Map.class))
                .thenReturn(Map.of("DocumentWithSignature", List.of(base64SignedDocument)));

        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(processedResponse);

        Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest);

        StepVerifier.create(result)
                .expectNext(processedResponse)
                .verifyComplete();
    }

    @Test
    void testRequestAccessTokenAccessTokenException() throws JsonProcessingException {
        signatureType = SignatureType.JADES;
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, Map.of());
        signatureRequest = new SignatureRequest(signatureConfiguration, "data");

        String invalidAccessTokenResponse = "{ \"invalid_key\": \"no_token_here\" }";

        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://api.external.com");

        doReturn(Mono.just(invalidAccessTokenResponse))
                .when(httpUtils)
                .postRequest(eq("https://api.external.com/oauth2/token"), any(List.class), anyString());

        when(objectMapper.readValue(invalidAccessTokenResponse, Map.class))
                .thenReturn(Map.of("invalid_key", "no_token_here"));

        Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest);

        StepVerifier.create(result)
                .expectError(AccessTokenException.class)
                .verify();
    }

    @Test
    void testProcessSignatureResponseSignatureProcessingException() throws JsonProcessingException {
        signatureType = SignatureType.JADES;
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, Map.of());
        signatureRequest = new SignatureRequest(signatureConfiguration, "data");

        String malformedSignatureResponse = "{ \"DocumentWithSignature\": [] }";

        when(objectMapper.readValue(malformedSignatureResponse, Map.class))
                .thenReturn(Map.of("DocumentWithSignature", List.of()));

        Mono<String> result = remoteSignatureService.processSignatureResponse(signatureRequest, malformedSignatureResponse);

        StepVerifier.create(result)
                .expectError(SignatureProcessingException.class)
                .verify();
    }



}
