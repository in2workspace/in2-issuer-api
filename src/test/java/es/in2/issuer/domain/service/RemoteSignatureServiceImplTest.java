//package es.in2.issuer.domain.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.in2.issuer.domain.model.SignatureConfiguration;
//import es.in2.issuer.domain.model.SignatureRequest;
//import es.in2.issuer.domain.model.enums.SignatureType;
//import es.in2.issuer.domain.model.SignedData;
//import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
//import es.in2.issuer.domain.util.HttpUtils;
//import es.in2.issuer.infrastructure.config.ApiConfig;
//import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.util.ReflectionTestUtils;
//import reactor.core.Exceptions;
//import reactor.core.publisher.Mono;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RemoteSignatureServiceImplTest {
//
//    @Mock
//    private RemoteSignatureConfig remoteSignatureConfig;
//
//    private ApiConfig apiConfig;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private HttpUtils httpUtils;
//
//    @InjectMocks
//    private RemoteSignatureServiceImpl remoteSignatureService;
//
//    @Test
//    void testInitializeRemoteSignatureBaseUrl() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        lenient().when(remoteSignatureConfig.getRemoteSignatureExternalDomain()).thenReturn(String.valueOf(Mono.just("dummyValue")));
//
//        Method privateMethod = RemoteSignatureServiceImpl.class.getDeclaredMethod("initializeRemoteSignatureBaseUrl");
//        privateMethod.setAccessible(true);
//
//        privateMethod.invoke(remoteSignatureService);
//
//        verify(remoteSignatureConfig, times(1)).getRemoteSignatureExternalDomain();
//    }
//
//    @Test
//    void testInitializeRemoteSignatureBaseUrlThrowsError() throws NoSuchMethodException {
//
//        Method privateMethod = RemoteSignatureServiceImpl.class.getDeclaredMethod("initializeRemoteSignatureBaseUrl");
//        privateMethod.setAccessible(true);
//
//        when(remoteSignatureConfig.getRemoteSignatureExternalDomain()).thenAnswer(invocation -> Mono.error(new RuntimeException("Simulated error")));
//
//        assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(remoteSignatureService));
//
//        verify(remoteSignatureConfig, times(1)).getRemoteSignatureExternalDomain();
//    }
//
//    @Test
//    void sign_Success() throws JsonProcessingException {
//
//        ReflectionTestUtils.setField(remoteSignatureService, "remoteSignatureBaseUrl", "http://baseurl");
//        ReflectionTestUtils.setField(remoteSignatureService, "sign", "/sign");
//
//        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE, new HashMap<>());
//        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, "data");
//        String expectedSignedDataJson = "{\"signature\":\"testSignature\",\"timestamp\":123456789}";
//        SignedData dto = SignedData.builder().build();
//
//        String url = "http://baseurl" + "/api/v1" + "/sign";
//
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
//
//        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn("signedSignature");
//        when(objectMapper.readValue(anyString(), eq(SignedData.class))).thenReturn(dto);
//        when(httpUtils.postRequest(url, headers, "signedSignature")).thenReturn(Mono.just(expectedSignedDataJson));
//
//        Mono<SignedData> resultMono = remoteSignatureService.sign(signatureRequest, token);
//        SignedData result = resultMono.block();
//
//        assertNotNull(result);
//
//        verify(httpUtils).postRequest(url, headers, "signedSignature");
//    }
//
//    @Test
//    void sign_JsonProcessingException_whenObjectMapper_writeValueAsString() throws JsonProcessingException {
//        ReflectionTestUtils.setField(remoteSignatureService, "remoteSignatureBaseUrl", "http://baseurl");
//        ReflectionTestUtils.setField(remoteSignatureService, "sign", "/sign");
//
//        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE, new HashMap<>());
//        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, "data");
//        String token = "testToken";
//        when(objectMapper.writeValueAsString(signatureRequest)).thenThrow(new JsonProcessingException("Json processing error") {
//        });
//
//        assertThrows(JsonProcessingException.class, () -> {
//            try {
//                remoteSignatureService.sign(signatureRequest, token).block();
//            } catch (Exception e) {
//                throw Exceptions.unwrap(e);
//            }
//        });
//
//        verifyNoInteractions(httpUtils);
//    }
//
//    @Test
//    void sign_JsonProcessingException_whenObjectMapper_readValue() throws JsonProcessingException {
//        // Set up
//        ReflectionTestUtils.setField(remoteSignatureService, "remoteSignatureBaseUrl", "http://baseurl");
//        ReflectionTestUtils.setField(remoteSignatureService, "sign", "/sign");
//
//        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE, new HashMap<>());
//        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, "data");
//        String expectedSignedDataJson = "{\"signature\":\"testSignature\",\"timestamp\":123456789}";
//
//        String url = "http://baseurl" + "/api/v1" + "/sign";
//
//        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
//
//        // Set up mocks
//        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn("signedSignature");
//        when(objectMapper.readValue(anyString(), eq(SignedData.class))).thenThrow(new JsonProcessingException("Json processing error") {
//        });
//        when(httpUtils.postRequest(url, headers, "signedSignature")).thenReturn(Mono.just(expectedSignedDataJson));
//
//        // Assert and verify
//        assertThrows(RuntimeException.class, () -> handleSignJsonProcessingError(remoteSignatureService, signatureRequest, token));
//        verify(httpUtils).postRequest(url, headers, "signedSignature");
//    }
//
//    private void handleSignJsonProcessingError(RemoteSignatureService remoteSignatureService, SignatureRequest signatureRequest, String token) {
//        try {
//            remoteSignatureService.sign(signatureRequest, token).block();
//        } catch (Exception e) {
//            throw new RuntimeException("Error signing data", e);
//        }
//    }
//
//}
