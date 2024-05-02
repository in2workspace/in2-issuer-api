package es.in2.issuer.application.service.impl;

import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.CredentialRequest;
import es.in2.issuer.domain.model.Proof;
import es.in2.issuer.domain.model.VerifiableCredentialResponse;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialIssuanceServiceImplTest {

    @Mock
    private RemoteSignatureService remoteSignatureService;
    @Mock
    private AuthenticSourcesRemoteService authenticSourcesRemoteService;
    @Mock
    private VerifiableCredentialService verifiableCredentialService;
    @Mock
    private AppConfiguration appConfiguration;
    @Mock
    private ProofValidationService proofValidationService;
    @Mock
    private CredentialManagementService credentialManagementService;
    @InjectMocks
    private VerifiableCredentialIssuanceServiceImpl service;

    String templateContent = "{\n" +
            "  \"type\": [\n" +
            "    \"VerifiableCredential\",\n" +
            "    \"LEARCredential\"\n" +
            "  ],\n" +
            "  \"@context\": [\n" +
            "    \"https://www.w3.org/2018/credentials/v1\",\n" +
            "    \"https://issueridp.dev.in2.es/2022/credentials/learcredential/v1\"\n" +
            "  ],\n" +
            "  \"id\": \"urn:uuid:84f6fe0b-7cc8-460e-bb54-f805f0984202\",\n" +
            "  \"issuer\": {\n" +
            "    \"id\": \"did:elsi:VATES-Q0801175A\"\n" +
            "  },\n" +
            "  \"issuanceDate\": \"2024-03-08T18:27:46Z\",\n" +
            "  \"issued\": \"2024-03-08T18:27:46Z\",\n" +
            "  \"validFrom\": \"2024-03-08T18:27:46Z\",\n" +
            "  \"expirationDate\": \"2024-04-07T18:27:45Z\",\n" +
            "  \"credentialSubject\": {}\n" +
            "}";

    @BeforeEach
    void setup() throws IOException {
        Resource mockResource = mock(Resource.class);
        lenient().when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8)));

        ReflectionTestUtils.setField(service, "learCredentialTemplate", mockResource);
    }
//    @Test
//    void testGenerateVerifiableCredentialResponse() throws UserDoesNotExistException {
//        String did = "did:key:zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC#zDnaen23wM76gpiSLHku4bFDbssVS9sty9x3K7yVqjbSdTPWC";
//        String jwtProof = "eyJraWQiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MjekRuYWVuMjN3TTc2Z3BpU0xIa3U0YkZEYnNzVlM5c3R5OXgzSzd5VnFqYlNkVFBXQyIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFlbjIzd003NmdwaVNMSGt1NGJGRGJzc1ZTOXN0eTl4M0s3eVZxamJTZFRQV0MiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTI5MTcwNDAsImlhdCI6MTcxMjA1MzA0MCwibm9uY2UiOiI4OVh4bXdMMlJtR2wyUlp1LU1UU3lRPT0ifQ.DdaaNm4vTn60njLtAQ7Q5oGsQILfA-5h9-sv4MBcVyNBAfSrUUajZqlUukT-5Bx8EqocSvf0RIFRHLcvO9_LMg";
//        String userId = "user123";
//        String token = "dummyToken";
//        String unsignedCredential = "unsignedCredential";
//        String transactionId = "1234";
//        CredentialRequest credentialRequest = CredentialRequest.builder()
//                .proof(
//                        Proof.builder().proofType("jwt").jwt(jwtProof).build())
//                .format(JWT_VC)
//                .build();
//        VerifiableCredentialResponse expectedResponse = VerifiableCredentialResponse.builder()
//                .credential(unsignedCredential)
//                .transactionId(transactionId)
//                .cNonce("89XxmwL2RmGl2RZu-MTSyQ==")
//                .cNonceExpiresIn(600)
//                .build();
//
//        when(proofValidationService.isProofValid(jwtProof)).thenReturn(Mono.just(true));
//        when(authenticSourcesRemoteService.getUserFromLocalFile()).thenReturn(Mono.just("userData"));
//        when(verifiableCredentialService.generateVc(eq(templateContent), eq(did), eq("did:example:issuer"), eq("userData"), any())).thenReturn(Mono.just(unsignedCredential));
//        when(credentialManagementService.commitCredential(unsignedCredential, userId,credentialRequest.format())).thenReturn(Mono.just(transactionId));
//        when(appConfiguration.getIssuerExternalDomain()).thenReturn("did:example:issuer");
//
//        StepVerifier.create(service.generateVerifiableCredentialResponse(userId, credentialRequest, token))
//                .assertNext(response -> assertEquals(expectedResponse, response))
//                .verifyComplete();
//
//    }
//    @Test
//    void generateVerifiableCredentialResponse_Success_JWTFormat() throws UserDoesNotExistException {
//        // Creating the inner map for LEARCredential data
//        Map<String, String> learCredentialData = new HashMap<>();
//        learCredentialData.put("id", "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh");
//        learCredentialData.put("first_name", "Francisco");
//        learCredentialData.put("last_name", "Pérez García");
//        learCredentialData.put("email", "francisco.perez@in2.es");
//        learCredentialData.put("serialnumber", "IDCES-46521781J");
//        learCredentialData.put("employeeType", "T2");
//        learCredentialData.put("organizational_unit", "GDI010034");
//        learCredentialData.put("organization", "GDI01");
//        // Creating the outer map for credentialSubjectData
//        Map<String, Map<String, String>> credentialSubjectData = new HashMap<>();
//        credentialSubjectData.put("LEARCredential", learCredentialData);
//        // Creating an instance of SubjectDataResponse using the builder
//        SubjectDataResponse subjectDataResponse = SubjectDataResponse.builder()
//                .credentialSubjectData(credentialSubjectData)
//                .build();
//
//        String jsonData = "{"
//                + "\"mandate\": {"
//                + "\"id\": \"4e3c02b8-5c57-4679-8aa5-502d62484af5\","
//                + "\"life_span\": {"
//                + "\"end_date_time\": \"2025-04-02 09:23:22.637345122 +0000 UTC\","
//                + "\"start_date_time\": \"2024-04-02 09:23:22.637345122 +0000 UTC\""
//                + "},"
//                + "\"mandatee\": {"
//                + "\"id\": \"did:key:zDnaeei6HxVe7ibR3mZmXa9SZgWs8UBj1FiTuwEKwmnChdUAu\","
//                + "\"email\": \"oriol.canades@in2.es\","
//                + "\"first_name\": \"Oriol\","
//                + "\"gender\": \"M\","
//                + "\"last_name\": \"Canadés\","
//                + "\"mobile_phone\": \"+34666336699\""
//                + "},"
//                + "\"mandator\": {"
//                + "\"commonName\": \"IN2\","
//                + "\"country\": \"ES\","
//                + "\"emailAddress\": \"rrhh@in2.es\","
//                + "\"organization\": \"IN2, Ingeniería de la Información, S.L.\","
//                + "\"organizationIdentifier\": \"VATES-B60645900\","
//                + "\"serialNumber\": \"B60645900\""
//                + "},"
//                + "\"power\": ["
//                + "{"
//                + "\"id\": \"6b8f3137-a57a-46a5-97e7-1117a20142fb\","
//                + "\"tmf_action\": \"Execute\","
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"Onboarding\","
//                + "\"tmf_type\": \"Domain\""
//                + "},"
//                + "{"
//                + "\"id\": \"ad9b1509-60ea-47d4-9878-18b581d8e19b\","
//                + "\"tmf_action\": [\"Create\", \"Update\"],"
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"ProductOffering\","
//                + "\"tmf_type\": \"Domain\""
//                + "}"
//                + "]"
//                + "}"
//                + "}";
//
//        String expectedCredential = "Credential";
//        when(authenticSourcesRemoteService.getUserFromLocalFile())
//                .thenReturn(Mono.just(jsonData)); // Mock the necessary user details
//        when(remoteSignatureService.sign(any(SignatureRequest.class), anyString()))
//                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, expectedCredential)));
//
//        // Define your test input here
//        String username = "testUser";
//        CredentialRequest request = new CredentialRequest("jwt_vc_json", new CredentialDefinition(List.of("")),new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ"));
//        String token = "testToken";
//
//        // Test the method
//        Mono<VerifiableCredentialResponse> result = service.generateVerifiableCredentialResponse(username, request, token);
//
//        // Verify the output
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assertEquals(expectedCredential, response.credential());
//                    // Additional assertions as necessary
//                })
//                .verifyComplete();
//
//        // Verify interactions
//        verify(authenticSourcesRemoteService).getUserFromLocalFile();
//        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token));
//    }
//
//    @Test
//    void generateVerifiableCredentialResponse_Success_CWTFormat() throws UserDoesNotExistException {
//        // Creating the inner map for LEARCredential data
//        Map<String, String> learCredentialData = new HashMap<>();
//        learCredentialData.put("id", "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh");
//        learCredentialData.put("first_name", "Francisco");
//        learCredentialData.put("last_name", "Pérez García");
//        learCredentialData.put("email", "francisco.perez@in2.es");
//        learCredentialData.put("serialnumber", "IDCES-46521781J");
//        learCredentialData.put("employeeType", "T2");
//        learCredentialData.put("organizational_unit", "GDI010034");
//        learCredentialData.put("organization", "GDI01");
//        // Creating the outer map for credentialSubjectData
//        Map<String, Map<String, String>> credentialSubjectData = new HashMap<>();
//        credentialSubjectData.put("LEARCredential", learCredentialData);
//        // Creating an instance of SubjectDataResponse using the builder
//        SubjectDataResponse subjectDataResponse = SubjectDataResponse.builder()
//                .credentialSubjectData(credentialSubjectData)
//                .build();
//
//        String jsonData = "{"
//                + "\"mandate\": {"
//                + "\"id\": \"4e3c02b8-5c57-4679-8aa5-502d62484af5\","
//                + "\"life_span\": {"
//                + "\"end_date_time\": \"2025-04-02 09:23:22.637345122 +0000 UTC\","
//                + "\"start_date_time\": \"2024-04-02 09:23:22.637345122 +0000 UTC\""
//                + "},"
//                + "\"mandatee\": {"
//                + "\"id\": \"did:key:zDnaeei6HxVe7ibR3mZmXa9SZgWs8UBj1FiTuwEKwmnChdUAu\","
//                + "\"email\": \"oriol.canades@in2.es\","
//                + "\"first_name\": \"Oriol\","
//                + "\"gender\": \"M\","
//                + "\"last_name\": \"Canadés\","
//                + "\"mobile_phone\": \"+34666336699\""
//                + "},"
//                + "\"mandator\": {"
//                + "\"commonName\": \"IN2\","
//                + "\"country\": \"ES\","
//                + "\"emailAddress\": \"rrhh@in2.es\","
//                + "\"organization\": \"IN2, Ingeniería de la Información, S.L.\","
//                + "\"organizationIdentifier\": \"VATES-B60645900\","
//                + "\"serialNumber\": \"B60645900\""
//                + "},"
//                + "\"power\": ["
//                + "{"
//                + "\"id\": \"6b8f3137-a57a-46a5-97e7-1117a20142fb\","
//                + "\"tmf_action\": \"Execute\","
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"Onboarding\","
//                + "\"tmf_type\": \"Domain\""
//                + "},"
//                + "{"
//                + "\"id\": \"ad9b1509-60ea-47d4-9878-18b581d8e19b\","
//                + "\"tmf_action\": [\"Create\", \"Update\"],"
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"ProductOffering\","
//                + "\"tmf_type\": \"Domain\""
//                + "}"
//                + "]"
//                + "}"
//                + "}";
//
//
//        String credential = "Credential";
//        String expectedCredential = "6BFA SZ-7INLM85C00K.5W0";
//        when(authenticSourcesRemoteService.getUserFromLocalFile())
//                .thenReturn(Mono.just(jsonData)); // Mock the necessary user details
//        when(remoteSignatureService.sign(any(SignatureRequest.class), anyString()))
//                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, credential)));
//
//        // Define your test input here
//        String username = "testUser";
//        CredentialRequest request = new CredentialRequest("cwt_vc", new CredentialDefinition(List.of("")), new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ"));
//        String token = "testToken";
//
//        // Test the method
//        Mono<VerifiableCredentialResponse> result = service.generateVerifiableCredentialResponse(username, request, token);
//
//        // Verify the output
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assertEquals(expectedCredential, response.credential());
//                    // Additional assertions as necessary
//                })
//                .verifyComplete();
//
//        // Verify interactions
//        verify(authenticSourcesRemoteService).getUserFromLocalFile();
//        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token));
//    }
//
//    @Test
//    void generateBatchVerifiableCredentialResponse_Success() throws UserDoesNotExistException {
//        // Creating the inner map for LEARCredential data
//        Map<String, String> learCredentialData = new HashMap<>();
//        learCredentialData.put("id", "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh");
//        learCredentialData.put("first_name", "Francisco");
//        learCredentialData.put("last_name", "Pérez García");
//        learCredentialData.put("email", "francisco.perez@in2.es");
//        learCredentialData.put("serialnumber", "IDCES-46521781J");
//        learCredentialData.put("employeeType", "T2");
//        learCredentialData.put("organizational_unit", "GDI010034");
//        learCredentialData.put("organization", "GDI01");
//        // Creating the outer map for credentialSubjectData
//        Map<String, Map<String, String>> credentialSubjectData = new HashMap<>();
//        credentialSubjectData.put("LEARCredential", learCredentialData);
//        // Creating an instance of SubjectDataResponse using the builder
//        SubjectDataResponse subjectDataResponse = SubjectDataResponse.builder()
//                .credentialSubjectData(credentialSubjectData)
//                .build();
//
//        String jsonData = "{"
//                + "\"mandate\": {"
//                + "\"id\": \"4e3c02b8-5c57-4679-8aa5-502d62484af5\","
//                + "\"life_span\": {"
//                + "\"end_date_time\": \"2025-04-02 09:23:22.637345122 +0000 UTC\","
//                + "\"start_date_time\": \"2024-04-02 09:23:22.637345122 +0000 UTC\""
//                + "},"
//                + "\"mandatee\": {"
//                + "\"id\": \"did:key:zDnaeei6HxVe7ibR3mZmXa9SZgWs8UBj1FiTuwEKwmnChdUAu\","
//                + "\"email\": \"oriol.canades@in2.es\","
//                + "\"first_name\": \"Oriol\","
//                + "\"gender\": \"M\","
//                + "\"last_name\": \"Canadés\","
//                + "\"mobile_phone\": \"+34666336699\""
//                + "},"
//                + "\"mandator\": {"
//                + "\"commonName\": \"IN2\","
//                + "\"country\": \"ES\","
//                + "\"emailAddress\": \"rrhh@in2.es\","
//                + "\"organization\": \"IN2, Ingeniería de la Información, S.L.\","
//                + "\"organizationIdentifier\": \"VATES-B60645900\","
//                + "\"serialNumber\": \"B60645900\""
//                + "},"
//                + "\"power\": ["
//                + "{"
//                + "\"id\": \"6b8f3137-a57a-46a5-97e7-1117a20142fb\","
//                + "\"tmf_action\": \"Execute\","
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"Onboarding\","
//                + "\"tmf_type\": \"Domain\""
//                + "},"
//                + "{"
//                + "\"id\": \"ad9b1509-60ea-47d4-9878-18b581d8e19b\","
//                + "\"tmf_action\": [\"Create\", \"Update\"],"
//                + "\"tmf_domain\": \"DOME\","
//                + "\"tmf_function\": \"ProductOffering\","
//                + "\"tmf_type\": \"Domain\""
//                + "}"
//                + "]"
//                + "}"
//                + "}";
//
//        String credential = "Credential";
//        String expectedCredential = "6BFA SZ-7INLM85C00K.5W0";
//        when(authenticSourcesRemoteService.getUserFromLocalFile())
//                .thenReturn(Mono.just(jsonData)); // Mock the necessary user details
//        when(remoteSignatureService.sign(any(SignatureRequest.class), anyString()))
//                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, credential)));
//
//        // Define your test input here
//        String username = "testUser";
//        CredentialRequest request1 = new CredentialRequest("jwt_vc", new CredentialDefinition(List.of("")),new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ"));
//        CredentialRequest request2 = new CredentialRequest("cwt_vc", new CredentialDefinition(List.of("")), new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ"));
//        BatchCredentialRequest batchCredentialRequest = new BatchCredentialRequest(List.of(request1, request2));
//        String token = "testToken";
//
//        // Test the method
//        Mono<BatchCredentialResponse> result = service.generateVerifiableCredentialBatchResponse(username, batchCredentialRequest, token);
//
//        // Verify the output
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assertEquals(2, response.credentialResponses().size());
//                    // Additional assertions as necessary
//                })
//                .verifyComplete();
//
//        // Verify interactions
//        verify(authenticSourcesRemoteService, times(2)).getUserFromLocalFile();
//        verify(remoteSignatureService, times(2)).sign(any(SignatureRequest.class), eq(token));
//    }

}