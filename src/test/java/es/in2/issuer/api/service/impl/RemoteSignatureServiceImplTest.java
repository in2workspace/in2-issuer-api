/*
package es.in2.issuer.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.api.config.azure.AppConfigurationKeys;
import es.in2.issuer.api.model.dto.SignatureConfiguration;
import es.in2.issuer.api.model.dto.SignatureRequest;
import es.in2.issuer.api.model.dto.SignedData;
import es.in2.issuer.api.model.enums.SignatureType;
import es.in2.issuer.api.service.AppConfigService;
import es.in2.issuer.api.util.HttpUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)

class RemoteSignatureServiceImplTest {

    @Mock
    private AppConfigService appConfigService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpUtils httpUtils;

    @InjectMocks
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Test
    void testInitializeRemoteSignatureBaseUrl() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        lenient().when(appConfigService.getConfiguration(any())).thenReturn(Mono.just("dummyValue"));

        Method privateMethod = RemoteSignatureServiceImpl.class.getDeclaredMethod("initializeRemoteSignatureBaseUrl");
        privateMethod.setAccessible(true);

        privateMethod.invoke(remoteSignatureService);

        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.CROSS_REMOTE_SIGNATURE_BASE_URL_KEY);
    }

    @Test
    void testInitializeRemoteSignatureBaseUrlThrowsError() throws NoSuchMethodException {

        Method privateMethod = RemoteSignatureServiceImpl.class.getDeclaredMethod("initializeRemoteSignatureBaseUrl");
        privateMethod.setAccessible(true);

        lenient().when(appConfigService.getConfiguration(AppConfigurationKeys.CROSS_REMOTE_SIGNATURE_BASE_URL_KEY)).thenReturn(Mono.error(new RuntimeException("Simulated error")));

        assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(remoteSignatureService));

        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.CROSS_REMOTE_SIGNATURE_BASE_URL_KEY);
    }

    @Test
    void sign_Success() throws JsonProcessingException {

        ReflectionTestUtils.setField(remoteSignatureService,"remoteSignatureBaseUrl","http://baseurl");
        ReflectionTestUtils.setField(remoteSignatureService,"sign","/sign");

        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE,new HashMap<>());
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration,"data");
        String expectedSignedDataJson = "{\"signature\":\"testSignature\",\"timestamp\":123456789}";
        SignedData dto = new SignedData();

        String url = "http://baseurl" + "/api/v1" + "/sign";

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn("signedSignature");
        when(objectMapper.readValue(anyString(), eq(SignedData.class))).thenReturn(dto);
        when(httpUtils.postRequest(url, headers, "signedSignature")).thenReturn(Mono.just(expectedSignedDataJson));

        Mono<SignedData> resultMono = remoteSignatureService.sign(signatureRequest, token);
        SignedData result = resultMono.block();

        assertNotNull(result);

        verify(httpUtils).postRequest(url, headers, "signedSignature");
    }

    @Test
    void sign_JsonProcessingException_whenObjectMapper_writeValueAsString() throws JsonProcessingException {
        ReflectionTestUtils.setField(remoteSignatureService,"remoteSignatureBaseUrl","http://baseurl");
        ReflectionTestUtils.setField(remoteSignatureService,"sign","/sign");

        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE,new HashMap<>());
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration,"data");
        String token = "testToken";
        when(objectMapper.writeValueAsString(signatureRequest)).thenThrow(new JsonProcessingException("Json processing error") {});

        assertThrows(JsonProcessingException.class, () -> {
            try {
                remoteSignatureService.sign(signatureRequest, token).block();
            } catch (Exception e) {
                throw Exceptions.unwrap(e);
            }
        });

        verifyNoInteractions(httpUtils);
    }

    @Test
    void sign_JsonProcessingException_whenObjectMapper_readValue() throws JsonProcessingException {
        ReflectionTestUtils.setField(remoteSignatureService,"remoteSignatureBaseUrl","http://baseurl");
        ReflectionTestUtils.setField(remoteSignatureService,"sign","/sign");

        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(SignatureType.COSE,new HashMap<>());
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration,"data");
        String expectedSignedDataJson = "{\"signature\":\"testSignature\",\"timestamp\":123456789}";

        String url = "http://baseurl" + "/api/v1" + "/sign";

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        when(objectMapper.writeValueAsString(signatureRequest)).thenReturn("signedSignature");
        when(objectMapper.readValue(anyString(), eq(SignedData.class))).thenThrow(new JsonProcessingException("Json processing error") {});
        when(httpUtils.postRequest(url, headers, "signedSignature")).thenReturn(Mono.just(expectedSignedDataJson));

        assertThrows(RuntimeException.class, () -> {
            try {
                remoteSignatureService.sign(signatureRequest, token).block();
            } catch (Exception e) {
                throw Exceptions.unwrap(e);
            }
        });
        verify(httpUtils).postRequest(url, headers, "signedSignature");

    }

}
*/