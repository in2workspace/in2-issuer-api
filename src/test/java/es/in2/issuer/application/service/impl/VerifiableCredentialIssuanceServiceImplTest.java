package es.in2.issuer.application.service.impl;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import es.in2.issuer.domain.exception.ExpiredCacheException;
import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.service.impl.VerifiableCredentialServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialIssuanceServiceImplTest {
    @Mock
    private RemoteSignatureService remoteSignatureService;

    @Mock
    private AuthenticSourcesRemoteService authenticSourcesRemoteService;

    @Mock
    private CacheStore<VerifiableCredentialJWT> credentialCacheStore;

    @Mock
    private CacheStore<String> cacheStore;

    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private VerifiableCredentialService verifiableCredentialService;

    @InjectMocks
    private VerifiableCredentialIssuanceServiceImpl service;

    @BeforeEach
    void setup() throws IOException {
        Resource mockResource = mock(Resource.class);
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
        lenient().when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8)));

        ReflectionTestUtils.setField(service, "learCredentialTemplate", mockResource);

        when(verifiableCredentialService.generateVcPayLoad(any(),any(),any(),any(),any()))
                .thenReturn(Mono.just("{\"type\": \"type\"}"));
    }


    @Test
    void generateVerifiableCredentialResponse_Success_JWTFormat() throws UserDoesNotExistException {
        // Creating the inner map for LEARCredential data
        Map<String, String> learCredentialData = new HashMap<>();
        learCredentialData.put("id", "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh");
        learCredentialData.put("first_name", "Francisco");
        learCredentialData.put("last_name", "Pérez García");
        learCredentialData.put("email", "francisco.perez@in2.es");
        learCredentialData.put("serialnumber", "IDCES-46521781J");
        learCredentialData.put("employeeType", "T2");
        learCredentialData.put("organizational_unit", "GDI010034");
        learCredentialData.put("organization", "GDI01");
        // Creating the outer map for credentialSubjectData
        Map<String, Map<String, String>> credentialSubjectData = new HashMap<>();
        credentialSubjectData.put("LEARCredential", learCredentialData);
        // Creating an instance of SubjectDataResponse using the builder
        SubjectDataResponse subjectDataResponse = SubjectDataResponse.builder()
                .credentialSubjectData(credentialSubjectData)
                .build();

        String expectedCredential = "Credential";
        when(authenticSourcesRemoteService.getUserFromLocalFile())
                .thenReturn(Mono.just(subjectDataResponse)); // Mock the necessary user details
        when(remoteSignatureService.sign(any(SignatureRequest.class), anyString()))
                .thenReturn(Mono.just(new SignedData(SignatureType.JADES, expectedCredential)));

        // Define your test input here
        String username = "testUser";
        CredentialRequest request = new CredentialRequest("jwt_vc", new CredentialDefinition(List.of("")),new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ")); // Fill in with appropriate test data
        String token = "testToken";

        // Test the method
        Mono<VerifiableCredentialResponse> result = service.generateVerifiableCredentialResponse(username, request, token);

        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(expectedCredential, response.credential());
                    // Additional assertions as necessary
                })
                .verifyComplete();

        // Verify interactions
        verify(authenticSourcesRemoteService).getUserFromLocalFile();
        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token));
    }

    @Test
    void generateVerifiableCredentialResponse_Success_CWTFormat() throws UserDoesNotExistException {
        // Creating the inner map for LEARCredential data
        Map<String, String> learCredentialData = new HashMap<>();
        learCredentialData.put("id", "did:key:zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh");
        learCredentialData.put("first_name", "Francisco");
        learCredentialData.put("last_name", "Pérez García");
        learCredentialData.put("email", "francisco.perez@in2.es");
        learCredentialData.put("serialnumber", "IDCES-46521781J");
        learCredentialData.put("employeeType", "T2");
        learCredentialData.put("organizational_unit", "GDI010034");
        learCredentialData.put("organization", "GDI01");
        // Creating the outer map for credentialSubjectData
        Map<String, Map<String, String>> credentialSubjectData = new HashMap<>();
        credentialSubjectData.put("LEARCredential", learCredentialData);
        // Creating an instance of SubjectDataResponse using the builder
        SubjectDataResponse subjectDataResponse = SubjectDataResponse.builder()
                .credentialSubjectData(credentialSubjectData)
                .build();

        String credential = "Credential";
        String expectedCredential = "6BFA SZ-7INLM85C00K.5W0";
        when(authenticSourcesRemoteService.getUserFromLocalFile())
                .thenReturn(Mono.just(subjectDataResponse)); // Mock the necessary user details
        when(remoteSignatureService.sign(any(SignatureRequest.class), anyString()))
                .thenReturn(Mono.just(new SignedData(SignatureType.COSE, credential)));

        // Define your test input here
        String username = "testUser";
        CredentialRequest request = new CredentialRequest("cwt_vc", new CredentialDefinition(List.of("")), new Proof("type","eyJraWQiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIjekRuYWV0eTQ2SzNtUWJIaXUzdVppUllYVlY0d1JOMmpNZjk1R1RqTWlTMVNuOHpEUiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0IiwiYWxnIjoiRVMyNTYifQ.eyJpc3MiOiJkaWQ6a2V5OnpEbmFldHk0NkszbVFiSGl1M3VaaVJZWFZWNHdSTjJqTWY5NUdUak1pUzFTbjh6RFIiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwNzEiLCJleHAiOjE3MTE3ODc2NDMsImlhdCI6MTcxMDkyMzY0Mywibm9uY2UiOiJYQm5NSHROSVF4S2owTGJ6emtjWjBRPT0ifQ.zGVUzEY77jxmxjblvhO4rtoidJWVy6BCXK5ajlGYbFNIIhSdgXFdMi6xOeiLb1R6O8_R7rooLRMVt-1byOlSnQ")); // Fill in with appropriate test data
        String token = "testToken";

        // Test the method
        Mono<VerifiableCredentialResponse> result = service.generateVerifiableCredentialResponse(username, request, token);

        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(expectedCredential, response.credential());
                    // Additional assertions as necessary
                })
                .verifyComplete();

        // Verify interactions
        verify(authenticSourcesRemoteService).getUserFromLocalFile();
        verify(remoteSignatureService).sign(any(SignatureRequest.class), eq(token));
    }
}