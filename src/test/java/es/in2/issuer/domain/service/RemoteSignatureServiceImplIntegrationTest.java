package es.in2.issuer.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RemoteSignatureServiceImplIntegrationTest {

    @Mock
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void testRealGetSignedDocumentExternal() {
        String jsonContent = "{\"sign\": \"signtest1234\"}";
        SignatureType signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, jsonContent);

        try {
            Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest);

            result.doOnSuccess(response -> {
                try {
                    Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
                    System.out.println("Processed response: " + responseMap);
                } catch (Exception e) {
                    System.err.println("Error processing response: " + e.getMessage());
                }
            }).doOnError(error -> {
                System.err.println("Error in the workflow: " + error.getMessage());
                error.printStackTrace();
            }).block();

        } catch (Exception e) {
            System.err.println("Error executing workflow: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
