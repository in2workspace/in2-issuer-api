package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    private SignatureRequest signatureRequest;
    private String token;
    private String signatureRemoteServerEndpoint;
    private SignatureType signatureType;

    @BeforeEach
    void setUp() {
        signatureType = SignatureType.COSE;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration1 = new SignatureConfiguration(signatureType, parameters);
        signatureRequest = new SignatureRequest(signatureConfiguration1, "data");
        token = "dummyToken";
        when(remoteSignatureConfig.getRemoteSignatureExternalDomain()).thenReturn("http://remote-signature.com");
        when(remoteSignatureConfig.getRemoteSignatureSignPath()).thenReturn("/sign");
        signatureRemoteServerEndpoint = "http://remote-signature.com/api/v1/sign";
    }

    @Test
    void testSignSuccess() throws JsonProcessingException {
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
        when(objectMapper.writeValueAsString(signatureRequest)).thenThrow(new JsonProcessingException("error") {
        });

        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token);

        StepVerifier.create(result)
                .expectError(JsonProcessingException.class)
                .verify();
    }

}