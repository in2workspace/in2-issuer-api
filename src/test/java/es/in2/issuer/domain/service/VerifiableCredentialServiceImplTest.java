//package es.in2.issuer.domain.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import es.in2.issuer.domain.service.impl.VerifiableCredentialServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import reactor.test.StepVerifier;
//
//import java.time.Instant;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class VerifiableCredentialServiceImplTest {
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @InjectMocks
//    private VerifiableCredentialServiceImpl verifiableCredentialService;
//
//    @Test
//    void testGenerateVcPayLoad() throws JsonProcessingException {
//        String vcTemplate = "{\"issuer\":\"did:example:123\",\"credentialSubject\":{}}";
//        String subjectDid = "did:example:456";
//        String issuerDid = "did:example:123";
//        String userData = "{\"name\":\"John Doe\"}";
//        Instant expiration = Instant.now().plusSeconds(3600);
//
//        // Create real ObjectMapper for node creation
//        ObjectMapper realMapper = new ObjectMapper();
//
//        // Mocking the template and user data JsonNode
//        JsonNode vcTemplateNode = realMapper.createObjectNode().put("issuer", issuerDid);
//        JsonNode userDataNode = realMapper.createObjectNode().put("name", "John Doe");
//
//        when(objectMapper.readTree(vcTemplate)).thenReturn(vcTemplateNode);
//        when(objectMapper.readTree(userData)).thenReturn(userDataNode);
//
//        // Use the real ObjectMapper to avoid issues with creating ObjectNode
//        when(objectMapper.createObjectNode()).thenAnswer(invocation -> realMapper.createObjectNode());
//
//        // Mocking final JSON output
//        String expectedUuid = UUID.randomUUID().toString();
//        String expectedJson = "{\"sub\":\"" + subjectDid + "\",\"vc\":{\"id\":\"" + expectedUuid + "\"}}";
//        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);
//
//        StepVerifier.create(verifiableCredentialService.generateVcPayLoad(vcTemplate, subjectDid, issuerDid, userData, expiration))
//                .expectNext(expectedJson)
//                .verifyComplete();
//    }
//
//    @Test
//    void testGenerateDeferredVcPayLoad() throws JsonProcessingException {
//        String vcTemplate = """
//        {
//            "id": "test-id",
//            "issuer": "did:example:123",
//            "credentialSubject": {
//                "mandate": {
//                        "mandatee": {
//                            "id": "did:example:456",
//                            "name": "Joe"
//                        }
//                    }
//            },
//            "validFrom": "2024-01-01T00:00:00Z",
//            "expirationDate": "2024-01-02T00:00:00Z"
//        }
//        """;
//
//
//        // Create real ObjectMapper for node creation
//        ObjectMapper realMapper = new ObjectMapper();
//
//        JsonNode mockVcTemplateNode = realMapper.readTree(vcTemplate);
//        when(objectMapper.readTree(vcTemplate)).thenReturn(mockVcTemplateNode);
//
//        String expectedJson = "{\"sub\":\"did:example:456\",\"nbf\":\"2024-01-01T00:00:00Z\",\"iss\":\"did:example:123\",\"exp\":\"2024-01-02T00:00:00Z\",\"iat\":\"2024-01-01T00:00:00Z\",\"jti\":\"test-id\",\"vc\":" + vcTemplate + "}";
//        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);
//        when(objectMapper.createObjectNode()).thenAnswer(invocation -> realMapper.createObjectNode());
//
//        StepVerifier.create(verifiableCredentialService.generateDeferredCredentialResponse(vcTemplate))
//                .expectNext(expectedJson)
//                .verifyComplete();
//    }
//
//
//    @Test
//    void testGenerateVc() throws JsonProcessingException {
//        String vcTemplate = "{\"issuer\":\"did:example:123\"}";
//        String subjectDid = "did:example:456";
//        String issuerDid = "did:example:123";
//        String userData = """
//        {
//            "mandate": {
//                "mandatee": {
//                    "id": "did:example:456",
//                    "name": "Joe"
//                }
//            }
//        }
//        """;
//        Instant expiration = Instant.now().plusSeconds(3600);
//
//        ObjectNode mandateeNode = new ObjectMapper().createObjectNode().put("id", "did:example:456");
//        mandateeNode.put("name", "Joe");
//        JsonNode mandateNode = new ObjectMapper().createObjectNode().set("mandatee", mandateeNode);
//        JsonNode mockUserDataNode = new ObjectMapper().createObjectNode().set("mandate", mandateNode);
//        JsonNode mockVcTemplateNode = new ObjectMapper().createObjectNode().put("issuer", issuerDid);
//
//        when(objectMapper.readTree(vcTemplate)).thenReturn(mockVcTemplateNode);
//        when(objectMapper.readTree(userData)).thenReturn(mockUserDataNode);
//
//        // Set up additional mock for the final serialization
//        String expectedJson = "{\"id\":\"urn:uuid:<UUID>\",\"issuer\":\"did:example:123\",\"issuanceDate\":\"<DATE>\",\"validFrom\":\"<DATE>\",\"expirationDate\":\"<DATE>\",\"credentialSubject\":{\"mandate\":\"did:example:456\",\"name\":\"John Doe\"}}";
//        when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson.replace("<UUID>", UUID.randomUUID().toString()).replace("<DATE>", Instant.now().toString()));
//
//        StepVerifier.create(verifiableCredentialService.retrieveVcAndBindMandateeId(vcTemplate, subjectDid, issuerDid, userData, expiration))
//                .expectNextMatches(json -> json.contains("John Doe") && json.contains(subjectDid))
//                .verifyComplete();
//    }
//
//
//}