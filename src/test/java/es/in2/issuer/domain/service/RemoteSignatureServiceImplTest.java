package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.AccessTokenException;
import es.in2.issuer.domain.exception.HashGenerationException;
import es.in2.issuer.domain.exception.SignatureProcessingException;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.domain.util.JwtUtils;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteSignatureServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpUtils httpUtils;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RemoteSignatureConfig remoteSignatureConfig;

    @InjectMocks
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Mock
    private HashGeneratorService hashGeneratorService;

    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    private SignatureRequest signatureRequest;
    private String token;
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
        String signatureRemoteServerEndpoint = "http://remote-signature-dss.com/api/v1/sign";

        JsonNode mockNode = mock(JsonNode.class);
        JsonNode vcNode = mock(JsonNode.class);
        when(mockNode.path("vc")).thenReturn(vcNode);
        when(vcNode.path("id")).thenReturn(mock(JsonNode.class));
        when(vcNode.path("id").asText()).thenReturn("test-id");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        String signatureRequestJSON = "{\"request\":\"data\"}";
        String signedResponse = "{\"signed\":\"data\"}";
        String data = "data";
        SignedData signedData = new SignedData(signatureType, data);

        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn(signatureRequestJSON);
        when(httpUtils.postRequest(eq(signatureRemoteServerEndpoint), any(), eq(signatureRequestJSON)))
                .thenReturn(Mono.just(signedResponse));
        when(objectMapper.readValue(signedResponse, SignedData.class)).thenReturn(signedData);

        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, "550e8400-e29b-41d4-a716-446655440000");

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

        JsonNode mockNode = mock(JsonNode.class);
        JsonNode vcNode = mock(JsonNode.class);
        when(mockNode.path("vc")).thenReturn(vcNode);
        when(vcNode.path("id")).thenReturn(mock(JsonNode.class));
        when(vcNode.path("id").asText()).thenReturn("test-id");
        when(objectMapper.readTree(anyString())).thenReturn(mockNode);

        when(objectMapper.writeValueAsString(any(SignatureRequest.class)))
                .thenThrow(new JsonProcessingException("error") {});


        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, "550e8400-e29b-41d4-a716-446655440000");

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
        String firstBase64SignedDocument = Base64.getEncoder().encodeToString("mock-signed-document".getBytes(StandardCharsets.UTF_8));
        String secondBase64SignedDocument = Base64.getEncoder().encodeToString(firstBase64SignedDocument.getBytes(StandardCharsets.UTF_8));
        String signedDocumentResponse = "{ \"DocumentWithSignature\": [\"" + firstBase64SignedDocument + "\"] }";
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
                .thenReturn(Map.of("DocumentWithSignature", List.of(secondBase64SignedDocument)));

        when(objectMapper.writeValueAsString(any(Map.class))).thenReturn(processedResponse);
        when(jwtUtils.decodePayload(firstBase64SignedDocument)).thenReturn("data");
        CredentialProcedure mockCredentialProcedure = mock(CredentialProcedure.class);

        Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest, "550e8400-e29b-41d4-a716-446655440000");

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

        Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest, "550e8400-e29b-41d4-a716-446655440000");

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

    @Test
    void testProcessSignatureResponse_ValidPayload() throws Exception {
        String originalData = "{\"sub\":\"test\",\"vc\":{\"id\":\"test-id\"}}";
        String firstEncode = "ey.eyJzdWIiOiJ0ZXN0IiwidmMiOnsiaWQiOiJ0ZXN0LWlkIn19.ey";
        String encodedData = "ZXkuZXlKemRXSWlPaUowWlhOMElpd2lkbU1pT25zaWFXUWlPaUowWlhOMExXbGtJbjE5LmV5";

        SignatureRequest mockRequest = mock(SignatureRequest.class);
        SignatureConfiguration mockConfig = mock(SignatureConfiguration.class);

        when(mockRequest.data()).thenReturn(originalData);
        when(mockRequest.configuration()).thenReturn(mockConfig);
        when(mockConfig.type()).thenReturn(SignatureType.JADES);

        when(jwtUtils.decodePayload(firstEncode)).thenReturn(originalData);

        String responseJson = "{\"DocumentWithSignature\": [\"" + encodedData + "\"]}";
        when(objectMapper.readValue(eq(responseJson), any(Class.class)))
                .thenReturn(Map.of("DocumentWithSignature", List.of(encodedData)));

        when(objectMapper.writeValueAsString(any(Map.class)))
                .thenReturn("{\"type\":\"JADES\",\"data\":\"" + firstEncode + "\"}");

        Mono<String> result = remoteSignatureService.processSignatureResponse(mockRequest, responseJson);

        StepVerifier.create(result)
                .expectNext("{\"type\":\"JADES\",\"data\":\"" + firstEncode + "\"}")
                .verifyComplete();

        verify(jwtUtils, times(1)).decodePayload(firstEncode);
        verify(mockRequest, times(1)).data();
    }

    @Test
    void testProcessSignatureResponse_InvalidPayload() throws Exception {
        String originalData = "{\"sub\":\"test\",\"vc\":{\"id\":\"test-id\"}}";
        String modifiedData = "{\"sub\":\"hacked\",\"vc\":{\"id\":\"test-id\"}}";
        String firstEncode = "ey.eyJzdWIiOiJ0ZXN0IiwidmMiOnsiaWQiOiJ0ZXN0LWlkIn19.ey";
        String encodedData = "ZXkuZXlKemRXSWlPaUowWlhOMElpd2lkbU1pT25zaWFXUWlPaUowWlhOMExXbGtJbjE5LmV5";

        SignatureRequest mockRequest = mock(SignatureRequest.class);
        when(mockRequest.data()).thenReturn(originalData);

        when(jwtUtils.decodePayload(firstEncode)).thenReturn(modifiedData);

        String responseJson = "{\"DocumentWithSignature\": [\"" + encodedData + "\"]}";
        when(objectMapper.readValue(eq(responseJson), any(Class.class)))
                .thenReturn(Map.of("DocumentWithSignature", List.of(encodedData)));

        Mono<String> result = remoteSignatureService.processSignatureResponse(mockRequest, responseJson);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof SignatureProcessingException &&
                        throwable.getMessage().equals("Signed payload received does not match the original data"))
                .verify();

        verify(jwtUtils, times(1)).decodePayload(firstEncode);
        verify(mockRequest, times(1)).data();
    }





}
