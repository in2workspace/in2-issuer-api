package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedData;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

    @Disabled("To test both methods, replace @Mock for @Autowired and @ExtendWith(MockitoExtension) for @SpringBootTest")
    @Test
    void testRealGetSignedDocumentExternal() {
        String jsonContent = "{\"sign\":\"signtest1234\"}";
        SignatureType signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, jsonContent);

        try {
            Mono<String> result = remoteSignatureService.getSignedDocumentExternal(signatureRequest);
            String response = result
                    .doOnSuccess(res -> {
                        try {
                            Map<String, Object> responseMap = objectMapper.readValue(res, Map.class);
                            System.out.println("Processed response: " + responseMap);
                        } catch (Exception e) {
                            System.err.println("Error processing response: " + e.getMessage());
                        }
                    })
                    .doOnError(error -> {
                        System.err.println("Error in the workflow: " + error.getMessage());
                        error.printStackTrace();
                    })
                    .block();

            Assertions.assertNotNull("The response from getSignedDocumentExternal should not be null.", response);

        } catch (Exception e) {
            System.err.println("Error executing workflow: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Disabled("To test both methods, replace @Mock for @Autowired and @ExtendWith(MockitoExtension) for @SpringBootTest")
    @Test
    void testSignFlow() throws JsonProcessingException {
        ObjectMapper objectMapperIntern = new ObjectMapper();

        String jsonString = objectMapperIntern.writeValueAsString(Map.of(
                "sub", "did:key:zDnaenW4mgDAQwSQKQ3hb8bmHPP6XfUmKQzWFhcLPr6Jd6EYG",
                "nbf", 1740646197,
                "iss", "did:elsi:VATEU-B99999999",
                "exp", 1772182197,
                "iat", 1740646197,
                "vc", Map.of(
                        "@context", new String[]{
                                "https://www.w3.org/ns/credentials/v2",
                                "https://trust-framework.dome-marketplace.eu/credentials/learcredentialemployee/v1"
                        },
                        "id", "16fed1dd-26dd-4300-872f-fbf07485b9cd",
                        "type", new String[]{"LEARCredentialEmployee", "VerifiableCredential"},
                        "credentialSubject", Map.of(
                                "mandate", Map.of(
                                        "id", "6473d610-ff27-4d47-ba29-9787616ac6f1",
                                        "life_span", Map.of(
                                                "end_date_time", "2026-02-27T08:49:57.938092811Z",
                                                "start_date_time", "2025-02-27T08:49:57.938092811Z"
                                        ),
                                        "mandatee", Map.of(
                                                "id", "did:key:zDnaenW4mgDAQwSQKQ3hb8bmHPP6XfUmKQzWFhcLPr6Jd6EYG",
                                                "email", "danielaocampoo@gmail.com",
                                                "first_name", "Demailtest",
                                                "last_name", "test",
                                                "mobile_phone", "+34 697817059"
                                        ),
                                        "mandator", Map.of(
                                                "commonName", "Jane Smith",
                                                "country", "Spain",
                                                "emailAddress", "janesmith@test.es",
                                                "organization", "IN2",
                                                "organizationIdentifier", "VATES-B60645900",
                                                "serialNumber", "IDCEU-78543692V"
                                        ),
                                        "power", new Object[]{
                                                Map.of(
                                                        "id", "a6158900-6ea3-4c0b-981c-e1bcfc0f56e7",
                                                        "tmf_action", "Execute",
                                                        "tmf_domain", "DOME",
                                                        "tmf_function", "Onboarding",
                                                        "tmf_type", "Domain"
                                                )
                                        },
                                        "signer", Map.of(
                                                "commonName", "ZEUS OLIMPOS",
                                                "country", "EU",
                                                "emailAddress", "domesupport@in2.es",
                                                "organization", "OLIMPO",
                                                "organizationIdentifier", "VATEU-B99999999",
                                                "serialNumber", "IDCEU-99999999P"
                                        )
                                )
                        ),
                        "issuer", "did:elsi:VATEU-B99999999",
                        "validFrom", "2025-02-27T08:49:57.938092811Z",
                        "validUntil", "2026-02-27T08:49:57.938092811Z"
                ),
                "jti", "24722a65-25df-464b-9e44-3a73d92ec666"
        ));
        SignatureType signatureType = SignatureType.JADES;
        Map<String, String> parameters = Map.of("param1", "value1", "param2", "value2");
        SignatureConfiguration signatureConfiguration = new SignatureConfiguration(signatureType, parameters);
        SignatureRequest signatureRequest = new SignatureRequest(signatureConfiguration, jsonString);

        String token = "mocked_token_123";
        String procedureId = "proc-12345";

        Mono<SignedData> result = remoteSignatureService.sign(signatureRequest, token, procedureId);

        SignedData signedData = result
                .doOnSuccess(res -> {
                    System.out.println("Successfully signed document: " + res);
                })
                .doOnError(error -> {
                    System.err.println("Error signing document: " + error.getMessage());
                    error.printStackTrace();
                })
                .block();

        Assertions.assertNotNull(signedData, "SignedData should not be null");
        Assertions.assertNotNull(signedData.data(), "Signature should be present");
    }
}
