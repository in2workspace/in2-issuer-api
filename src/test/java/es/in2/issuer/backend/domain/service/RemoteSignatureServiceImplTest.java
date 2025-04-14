package es.in2.issuer.backend.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.domain.exception.*;
import es.in2.issuer.backend.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.backend.domain.model.dto.SignatureRequest;
import es.in2.issuer.backend.domain.model.dto.SignedData;
import es.in2.issuer.backend.domain.model.entities.CredentialProcedure;
import es.in2.issuer.backend.domain.model.entities.DeferredCredentialMetadata;
import es.in2.issuer.backend.domain.model.enums.CredentialStatus;
import es.in2.issuer.backend.domain.model.enums.SignatureType;
import es.in2.issuer.backend.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.backend.domain.util.HttpUtils;
import es.in2.issuer.backend.domain.util.JwtUtils;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.backend.infrastructure.config.RemoteSignatureConfig;
import es.in2.issuer.backend.infrastructure.repository.CredentialProcedureRepository;
import es.in2.issuer.backend.infrastructure.repository.DeferredCredentialMetadataRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

import static es.in2.issuer.backend.domain.util.Constants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Spy
    @InjectMocks
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Mock
    private HashGeneratorService hashGeneratorService;

    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Mock
    private CredentialProcedureRepository credentialProcedureRepository;

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;

    @Mock
    private AppConfig appConfig;

    @Mock
    private EmailService emailService;

    private SignatureRequest signatureRequest;
    private String token;
    private SignatureType signatureType;
    private final String mockAccessToken = "mockAccessToken";
    private final String mockCredentialID = "mockCredentialID";
    private final String mockCredentialListEndpoint = "https://remote-signature.com/csc/v2/credentials/list";
    private final String mockCredentialInfoEndpoint = "https://remote-signature.com/csc/v2/credentials/info";

    @Test
    void testSignSuccessDSS() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_SERVER);
        signatureType = SignatureType.COSE;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration1 = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration1, "data");
        token = "dummyToken";
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature-dss.com");
        when(remoteSignatureConfig.getRemoteSignatureSignPath()).thenReturn("/sign");
        String signatureRemoteServerEndpoint = "http://remote-signature-dss.com/api/v1/sign";

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
    void testSignRemoteSignatureException() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_SERVER);
        signatureType = SignatureType.COSE;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration1 = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration1, "");
        token = "dummyToken";
        when(objectMapper.writeValueAsString(any())).thenReturn("");
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature-dss.com");
        when(remoteSignatureConfig.getRemoteSignatureSignPath()).thenReturn("/sign");
        when(httpUtils.postRequest(eq("http://remote-signature-dss.com/api/v1/sign"), any(), anyString()))
                .thenReturn(Mono.error(new RemoteSignatureException("Error serializing signature request")));
        doReturn(Mono.empty()).when(remoteSignatureService).handlePostRecoverError(anyString());

        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, "550e8400-e29b-41d4-a716-446655440000");

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(RemoteSignatureException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Signature Failed, changed to ASYNC mode");
                })
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

        when(jwtUtils.areJsonsEqual(anyString(), anyString())).thenReturn(true);
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
        when(jwtUtils.areJsonsEqual(anyString(), anyString())).thenReturn(true);
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

    @Test
    void testHandlePostRecoverError_SuccessfulUpdate() throws Exception {
        UUID procedureUUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        CredentialProcedure procedure = mock(CredentialProcedure.class);
        DeferredCredentialMetadata deferredProcedure = mock(DeferredCredentialMetadata.class);

        when(credentialProcedureRepository.findByProcedureId(procedureUUID))
                .thenReturn(Mono.just(procedure));
        when(credentialProcedureRepository.save(any(CredentialProcedure.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(deferredCredentialMetadataRepository.findByProcedureId(procedureUUID))
                .thenReturn(Mono.just(deferredProcedure));
        when(deferredCredentialMetadataRepository.save(any(DeferredCredentialMetadata.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(appConfig.getIssuerUiExternalDomain()).thenReturn("http://issuer-ui.com");
        when(credentialProcedureService.getSignerEmailFromDecodedCredentialByProcedureId(procedureUUID.toString()))
                .thenReturn(Mono.just(""));
        when(emailService.sendPendingSignatureCredentialNotification(anyString(), anyString(), eq(procedureUUID.toString()), eq("http://issuer-ui.com")))
                .thenReturn(Mono.empty());

        Method method = RemoteSignatureServiceImpl.class.getDeclaredMethod("handlePostRecoverError", String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Mono<String> resultMono = (Mono<String>) method.invoke(remoteSignatureService, procedureUUID.toString());

        StepVerifier.create(resultMono)
                .expectComplete()
                .verify();

        verify(credentialProcedureRepository).findByProcedureId(procedureUUID);
        verify(credentialProcedureRepository).save(procedure);
        verify(deferredCredentialMetadataRepository).findByProcedureId(procedureUUID);
        verify(deferredCredentialMetadataRepository).save(deferredProcedure);

        verify(procedure).setOperationMode(ASYNC);
        verify(procedure).setCredentialStatus(CredentialStatus.PEND_SIGNATURE);
        verify(deferredProcedure).setOperationMode(ASYNC);
    }
    
    @Test
    void testSignSuccessOnFirstAttempt() throws JsonProcessingException {
        // Arrange
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration, "\"vc\": {\"id\": \"fa7376e0-fcc1-44c0-a91e-001a1301c06e\"}");
        token = "dummyToken";
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";

        // Configure server endpoint
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature.com");
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn("id1");
        when(httpUtils.postRequest(eq("http://remote-signature.com/oauth2/token"), any(), anyString()))
                .thenReturn(Mono.just("{\"access_token\": \"mockAccessToken\"}"));

        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("access_token", "mockAccessToken");

        when(objectMapper.readValue("{\"access_token\": \"mockAccessToken\"}", Map.class)).thenReturn(mockResponseMap);

        when(httpUtils.postRequest(eq("http://remote-signature.com/csc/v2/signatures/signDoc"), any(), any()))
                .thenReturn(Mono.just("{DocumentWithSignature: [ZGF0YQo=]}"));
        when(jwtUtils.areJsonsEqual(anyString(), anyString())).thenReturn(true);
        Map<String, List<String>> mockResponseMap2 = new HashMap<>();
        mockResponseMap2.put("DocumentWithSignature", List.of("ZGF0YQo="));

        when(objectMapper.readValue("{DocumentWithSignature: [ZGF0YQo=]}", Map.class)).thenReturn(mockResponseMap2);

        when(jwtUtils.decodePayload(any())).thenReturn("\"vc\": {\"id\": \"fa7376e0-fcc1-44c0-a91e-001a1301c06e\"}");

        // Act
        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, procedureId);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        // Verify deferredCredentialMetadataService was called to delete the metadata
        verify(deferredCredentialMetadataService).deleteDeferredCredentialMetadataById(procedureId);

        // Verify handlePostRecoverError was not called (no recovery needed)
        verify(credentialProcedureRepository, never()).findByProcedureId(any(UUID.class));
        verify(deferredCredentialMetadataRepository, never()).findByProcedureId(any(UUID.class));
    }
    
    @Test
    void testSignSuccessAfterRetries() throws JsonProcessingException {
        // Arrange
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration, "\"vc\": {\"id\": \"fa7376e0-fcc1-44c0-a91e-001a1301c06e\"}");
        token = "dummyToken";
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";

        // Create a server error response
        WebClientResponseException serverError = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                HttpHeaders.EMPTY,
                null,
                null
        );

        // Configure server endpoint
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature.com");
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn("id1");

        when(httpUtils.postRequest(eq("http://remote-signature.com/oauth2/token"), any(), anyString()))
                .thenReturn(Mono.just("{\"access_token\": \"mockAccessToken\"}"));

        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("access_token", "mockAccessToken");

        when(objectMapper.readValue("{\"access_token\": \"mockAccessToken\"}", Map.class)).thenReturn(mockResponseMap);

        when(httpUtils.postRequest(eq("http://remote-signature.com/csc/v2/signatures/signDoc"), any(), any()))
                .thenReturn(
                        Mono.error(serverError), // First attempt - fail
                        Mono.error(serverError), // Second attempt - fail
                        Mono.just("{DocumentWithSignature: [ZGF0YQo=]}") // Third attempt - success
                );

        Map<String, List<String>> mockResponseMap2 = new HashMap<>();
        mockResponseMap2.put("DocumentWithSignature", List.of("ZGF0YQo="));

        when(jwtUtils.areJsonsEqual(anyString(), anyString())).thenReturn(true);

        when(objectMapper.readValue("{DocumentWithSignature: [ZGF0YQo=]}", Map.class)).thenReturn(mockResponseMap2);

        when(jwtUtils.decodePayload(any())).thenReturn("\"vc\": {\"id\": \"fa7376e0-fcc1-44c0-a91e-001a1301c06e\"}");

        // Act
        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, procedureId);

        // Assert
        StepVerifier.create(result)
                .expectComplete()
                .verify();

        verify(httpUtils, times(4)).postRequest(any(), any(), any());
        // Verify deferredCredentialMetadataService was called to delete the metadata
        verify(deferredCredentialMetadataService).deleteDeferredCredentialMetadataById(procedureId);

        // Verify handlePostRecoverError was not called (no recovery needed)
        verify(credentialProcedureRepository, never()).findByProcedureId(any(UUID.class));
        verify(deferredCredentialMetadataRepository, never()).findByProcedureId(any(UUID.class));
    }
    
    @Test
    void testSignFailAfterAllRetries() throws JsonProcessingException {
        // Arrange
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration, "\"vc\": {\"id\": \"fa7376e0-fcc1-44c0-a91e-001a1301c06e\"}");
        token = "dummyToken";
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        UUID procedureUUID = UUID.fromString(procedureId);

        // Configure server endpoint
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("http://remote-signature.com");
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn("id1");

        when(httpUtils.postRequest(eq("http://remote-signature.com/csc/v2/credentials/list"), any(), anyString()))
                .thenReturn(Mono.just("{\"credentialIDs\": [\"id1\", \"id2\"]}"));

        when(httpUtils.postRequest(eq("http://remote-signature.com/oauth2/token"), any(), anyString()))
                .thenReturn(
                        Mono.just("{\"access_token\": \"mockAccessToken\"}"), //Service token success
                        Mono.error(new RuntimeException("First attempt failed")), //Sign token fail
                        Mono.error(new RuntimeException("Second attempt failed")), //Sign token fail
                        Mono.error(new RuntimeException("Third attempt failed")) //Sign token fail
                );
        Map<String, Object> mockResponseMap = new HashMap<>();
        mockResponseMap.put("access_token", "mockAccessToken");

        when(objectMapper.readValue("{\"access_token\": \"mockAccessToken\"}", Map.class)).thenReturn(mockResponseMap);

        // Mock entity repositories for error recovery
        CredentialProcedure procedure = new CredentialProcedure();
        DeferredCredentialMetadata deferredMetadata = new DeferredCredentialMetadata();

        when(credentialProcedureRepository.findByProcedureId(procedureUUID))
                .thenReturn(Mono.just(procedure));
        when(credentialProcedureRepository.save(any(CredentialProcedure.class)))
                .thenReturn(Mono.just(procedure));

        when(deferredCredentialMetadataRepository.findByProcedureId(procedureUUID))
                .thenReturn(Mono.just(deferredMetadata));
        when(deferredCredentialMetadataRepository.save(any(DeferredCredentialMetadata.class)))
                .thenReturn(Mono.just(deferredMetadata));
        when(appConfig.getIssuerUiExternalDomain()).thenReturn("http://issuer-ui.com");
        when(credentialProcedureService.getSignerEmailFromDecodedCredentialByProcedureId(procedureUUID.toString()))
                .thenReturn(Mono.just(""));
        when(emailService.sendPendingSignatureCredentialNotification(anyString(), anyString(), eq(procedureUUID.toString()), eq("http://issuer-ui.com")))
                .thenReturn(Mono.empty());
        // Act
        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, procedureId);

        // Assert
        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    // Verify error type and message
                    assertThat(throwable).isInstanceOf(RemoteSignatureException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Signature Failed, changed to ASYNC mode");
                    assertThat(throwable.getCause()).isNotNull();
                })
                .verify();

        // Verify the post request was attempted at least 3 times
        verify(httpUtils, atLeast(2)).postRequest(any(), any(), any());

        // Verify entities were updated to ASYNC mode and PEND_SIGNATURE status
        verify(credentialProcedureRepository).findByProcedureId(procedureUUID);
        verify(credentialProcedureRepository).save(procedure);

        verify(deferredCredentialMetadataRepository).findByProcedureId(procedureUUID);
        verify(deferredCredentialMetadataRepository).save(deferredMetadata);

        // Verify deferredCredentialMetadataService.delete was NOT called (we're in error recovery)
        verify(deferredCredentialMetadataService, never()).deleteDeferredCredentialMetadataById(anyString());

        Assertions.assertEquals(ASYNC, procedure.getOperationMode());
        Assertions.assertEquals(CredentialStatus.PEND_SIGNATURE, procedure.getCredentialStatus());
        Assertions.assertEquals(ASYNC, deferredMetadata.getOperationMode());

    }

    @Test
    void testValidateCertificate_ValidCertificate() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID.trim());
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        Map<String, List<String>> mockResponse = new HashMap<>();
        mockResponse.put("credentialIDs", new ArrayList<>(List.of(mockCredentialID)));
        objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(mockResponse);

        when(httpUtils.postRequest(eq("https://remote-signature.com/csc/v2/credentials/list"), any(), any()))
                .thenReturn(Mono.justOrEmpty(responseJson));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(httpUtils, times(1)).postRequest(eq(mockCredentialListEndpoint), anyList(), anyString());
    }

    @Test
    void testValidateCertificate_InvalidCertificate() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        Map<String, List<String>> mockResponse = Map.of("credentialIDs", List.of("otherCredentialID"));
        String responseJson = objectMapper.writeValueAsString(mockResponse);

        when(httpUtils.postRequest(eq(mockCredentialListEndpoint), anyList(), anyString()))
                .thenReturn(Mono.justOrEmpty(responseJson));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(httpUtils, times(1)).postRequest(eq(mockCredentialListEndpoint), anyList(), anyString());
    }

    @Test
    void testValidateCertificate_NullCredentialList() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        Map<String, List<String>> mockResponse = new HashMap<>();
        mockResponse.put("credentialIDs", null);
        String responseJson = objectMapper.writeValueAsString(mockResponse);

        when(httpUtils.postRequest(eq(mockCredentialListEndpoint), anyList(), anyString()))
                .thenReturn(Mono.justOrEmpty(responseJson));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testValidateCertificate_MissingCredentialIDsKey() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        Map<String, String> mockResponse = Map.of("otherKey", "someValue");
        String responseJson = objectMapper.writeValueAsString(mockResponse);

        when(httpUtils.postRequest(eq(mockCredentialListEndpoint), anyList(), anyString()))
                .thenReturn(Mono.justOrEmpty(responseJson));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void testValidateCertificate_InvalidJsonResponse() {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        when(httpUtils.postRequest(eq(mockCredentialListEndpoint), anyList(), anyString()))
                .thenReturn(Mono.justOrEmpty("invalid-json"));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectError(RemoteSignatureException.class)
                .verify();

        verify(httpUtils, times(1)).postRequest(eq(mockCredentialListEndpoint), anyList(), anyString());
    }

    @Test
    void testValidateCertificate_HttpRequestFailure() {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        when(httpUtils.postRequest(eq(mockCredentialListEndpoint), anyList(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("HTTP error")));

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(httpUtils, times(1)).postRequest(eq(mockCredentialListEndpoint), anyList(), anyString());
    }

    @Test
    void testValidateCertificate_JsonProcessingException() {
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn(mockCredentialID);
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");

        doAnswer(invocation -> {
            throw new JsonProcessingException("Error processing JSON") {
            };
        }).when(httpUtils).postRequest(eq(mockCredentialListEndpoint), anyList(), anyString());

        Mono<Boolean> result = remoteSignatureService.validateCertificate(mockAccessToken);

        StepVerifier.create(result)
                .expectError(RemoteSignatureException.class)
                .verify();
    }

    @Test
    void requestCertificateInfo_Success() throws JsonProcessingException {
        String requestBody = "{\"credentialID\":\"" + mockCredentialID + "\",\"certificates\":\"chain\",\"certInfo\":\"true\",\"authInfo\":\"true\"}";

        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        when(objectMapper.writeValueAsString(any())).thenReturn(requestBody);
        String mockCertificateResponse = "certificate-info-response";
        when(httpUtils.postRequest(eq(mockCredentialInfoEndpoint), anyList(), eq(requestBody)))
                .thenReturn(Mono.just(mockCertificateResponse));

        StepVerifier.create(remoteSignatureService.requestCertificateInfo(mockAccessToken, mockCredentialID))
                .expectNext(mockCertificateResponse)
                .verifyComplete();
    }

    @Test
    void requestCertificateInfo_SerializationError() throws JsonProcessingException {
        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Serialization failed") {
        });

        StepVerifier.create(remoteSignatureService.requestCertificateInfo(mockAccessToken, mockCredentialID))
                .expectErrorMessage("Error serializing request body to JSON")
                .verify();
    }

    @Test
    void requestCertificateInfo_HttpError() throws JsonProcessingException {
        String requestBody = "{\"credentialID\":\"" + mockCredentialID + "\",\"certificates\":\"chain\",\"certInfo\":\"true\",\"authInfo\":\"true\"}";

        when(remoteSignatureConfig.getRemoteSignatureDomain()).thenReturn("https://remote-signature.com");
        when(objectMapper.writeValueAsString(any())).thenReturn(requestBody);
        when(httpUtils.postRequest(eq(mockCredentialInfoEndpoint), anyList(), eq(requestBody)))
                .thenReturn(Mono.error(new RuntimeException("HTTP request failed")));

        StepVerifier.create(remoteSignatureService.requestCertificateInfo(mockAccessToken, mockCredentialID))
                .expectErrorMessage("HTTP request failed")
                .verify();
    }

    @Test
    void extractIssuerFromCertificateInfo_Success() throws JsonProcessingException {
        String certificateInfo = "{ \"cert\": { \"subjectDN\": \"CN=John Doe,O=Company,C=US\", \"serialNumber\": \"12345\", \"certificates\": [\"b3JnYW5pemF0aW9uSWRlbnRpZmllcj1vcmc=\"] } }";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode certificateInfoNode = realObjectMapper.readTree(certificateInfo);

        when(objectMapper.readTree(certificateInfo)).thenReturn(certificateInfoNode);
        StepVerifier.create(remoteSignatureService.extractIssuerFromCertificateInfo(certificateInfo, "john@example.com"))
                .assertNext(issuer -> {
                    Assertions.assertEquals("did:elsi:org", issuer.id());
                    Assertions.assertEquals("org", issuer.organizationIdentifier());
                    Assertions.assertEquals("Company", issuer.organization());
                    Assertions.assertEquals("US", issuer.country());
                    Assertions.assertEquals("John Doe", issuer.commonName());
                    Assertions.assertEquals("john@example.com", issuer.emailAddress());
                    Assertions.assertEquals("12345", issuer.serialNumber());
                })
                .verifyComplete();
    }

    @Test
    void extractIssuerFromCertificateInfo_JsonProcessingError() throws JsonProcessingException {
        String certificateInfo = "invalid-json";

        when(objectMapper.readTree(certificateInfo)).thenThrow(new JsonProcessingException("JSON parse error") {
        });

        StepVerifier.create(remoteSignatureService.extractIssuerFromCertificateInfo(certificateInfo, "procedureId"))
                .expectErrorMessage("Error parsing certificate info")
                .verify();
    }

    @Test
    void extractIssuerFromCertificateInfo_InvalidSubjectDN() throws JsonProcessingException {
        String certificateInfo = "{ \"cert\": { \"subjectDN\": \"invalid-dn\", \"serialNumber\": \"12345\" } }";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode certificateInfoNode = realObjectMapper.readTree(certificateInfo);

        when(objectMapper.readTree(certificateInfo)).thenReturn(certificateInfoNode);

        StepVerifier.create(remoteSignatureService.extractIssuerFromCertificateInfo(certificateInfo, "procedureId"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(RuntimeException.class, throwable);
                    Assertions.assertEquals("Error parsing subjectDN", throwable.getMessage());
                })
                .verify();
    }

    @Test
    void extractIssuerFromCertificateInfo_OrganizationIdentifierNotFound() throws JsonProcessingException {
        String certificateInfo = "{ \"cert\": { \"subjectDN\": \"CN=John Doe,O=Company,C=US\", \"serialNumber\": \"12345\", \"certificates\": [] } }";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode certificateInfoNode = realObjectMapper.readTree(certificateInfo);

        when(objectMapper.readTree(certificateInfo)).thenReturn(certificateInfoNode);

        StepVerifier.create(remoteSignatureService.extractIssuerFromCertificateInfo(certificateInfo, "procedureId"))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(OrganizationIdentifierNotFoundException.class, throwable);
                    Assertions.assertEquals("organizationIdentifier not found in the certificate.", throwable.getMessage());
                })
                .verify();

    }
    @Test
    void extractIssuerFromCertificateInfo_Success_X509Branch() throws Exception {
        String dummyCertContent = "-----BEGIN CERTIFICATE-----\nMIIDummyCertificateContent\n-----END CERTIFICATE-----";
        String base64Cert = Base64.getEncoder().encodeToString(dummyCertContent.getBytes(StandardCharsets.UTF_8));
        String certificateInfo = "{ \"cert\": { " +
                "\"subjectDN\": \"CN=John Doe,O=Company,C=US\", " +
                "\"serialNumber\": \"12345\", " +
                "\"certificates\": [\"" + base64Cert + "\"] } }";

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode certificateInfoNode = realObjectMapper.readTree(certificateInfo);
        when(objectMapper.readTree(certificateInfo)).thenReturn(certificateInfoNode);
        X509Certificate mockCert = mock(X509Certificate.class);
        when(mockCert.toString()).thenReturn(
                "Version: V3, Subject: C=ES, L=Barcelona, O=IN2, OID.2.5.4.97=TESTVAT, SERIALNUMBER=TESTVAT, CN=Seal Signature Credentials in SBX for testing");

        try (MockedStatic<CertificateFactory> mockedFactory = Mockito.mockStatic(CertificateFactory.class)) {
            CertificateFactory mockCf = mock(CertificateFactory.class);
            mockedFactory.when(() -> CertificateFactory.getInstance("X.509")).thenReturn(mockCf);
            when(mockCf.generateCertificate(any(InputStream.class))).thenReturn(mockCert);

            StepVerifier.create(remoteSignatureService.extractIssuerFromCertificateInfo(certificateInfo, "john@example.com"))
                    .assertNext(issuer -> {
                        Assertions.assertEquals("did:elsi:TESTVAT", issuer.id());
                        Assertions.assertEquals("TESTVAT", issuer.organizationIdentifier());
                        Assertions.assertEquals("Company", issuer.organization());
                        Assertions.assertEquals("US", issuer.country());
                        Assertions.assertEquals("John Doe", issuer.commonName());
                        Assertions.assertEquals("john@example.com", issuer.emailAddress());
                        Assertions.assertEquals("12345", issuer.serialNumber());
                    })
                    .verifyComplete();
        }
    }
    @Test
    void extractOrgFromX509_NoOrganizationIdentifier() throws Exception {
        byte[] dummyBytes = "dummy".getBytes(StandardCharsets.UTF_8);
        X509Certificate mockCert = mock(X509Certificate.class);
        when(mockCert.toString()).thenReturn("Version: V3, Subject: C=ES, O=Company, CN=John Doe");
        CertificateFactory mockCf = mock(CertificateFactory.class);
        when(mockCf.generateCertificate(any(ByteArrayInputStream.class))).thenReturn(mockCert);

        try (MockedStatic<CertificateFactory> mockedFactory = Mockito.mockStatic(CertificateFactory.class)) {
            mockedFactory.when(() -> CertificateFactory.getInstance("X.509")).thenReturn(mockCf);

            Mono<String> result = remoteSignatureService.extractOrgFromX509(dummyBytes);
            StepVerifier.create(result)
                    .expectComplete()
                    .verify();
        }
    }

    @Test
    void extractOrgFromX509_Exception() throws Exception {
        byte[] dummyBytes = "dummy".getBytes(StandardCharsets.UTF_8);
        CertificateFactory mockCf = mock(CertificateFactory.class);
        when(mockCf.generateCertificate(any(ByteArrayInputStream.class)))
                .thenThrow(new RuntimeException("Test exception"));

        try (MockedStatic<CertificateFactory> mockedFactory = Mockito.mockStatic(CertificateFactory.class)) {
            mockedFactory.when(() -> CertificateFactory.getInstance("X.509")).thenReturn(mockCf);

            Mono<String> result = remoteSignatureService.extractOrgFromX509(dummyBytes);
            StepVerifier.create(result)
                    .expectComplete()
                    .verify();
        }
    }

}
