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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.Map;

@SpringBootTest
class RemoteSignatureServiceImplIntegrationTest {

    @Autowired
    private RemoteSignatureServiceImpl remoteSignatureService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRealGetSignedDocumentExternal() {
        // Definir JSON de prueba
        String jsonContent = "{\"sign\": \"signtest1234\"}";
        SignatureType signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, jsonContent);

        try {
            // Llamada al servicio con logs adicionales para depuración
            Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest)
                    .doOnSubscribe(subscription -> System.out.println("🔹 Request sent: " + jsonContent))
                    .doOnSuccess(response -> System.out.println("✅ Response received: " + response))
                    .doOnError(error -> {
                        System.err.println("❌ Error in the workflow: " + error.getMessage());
                        error.printStackTrace();
                    });

            // Bloquear para esperar la respuesta
            result.block();

        } catch (Exception e) {
            System.err.println("🚨 Error executing workflow: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
