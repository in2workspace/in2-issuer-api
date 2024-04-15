package es.in2.issuer.domain.service;

import static es.in2.issuer.domain.util.Constants.CREDENTIAL_SUBJECT;
import static org.junit.jupiter.api.Assertions.*;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.impl.VerifiableCredentialServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class VerifiableCredentialServiceImplTest {
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

    @InjectMocks
    private VerifiableCredentialServiceImpl service;

    @Test
    void generateVcPayloadTest(){
        String subjectDid = "subjectDid";
        String issuerDid = "issuerDid";
        Instant expiration = Instant.now().plus(30, ChronoUnit.DAYS);
        // Creating template
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
        // Creating userData
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
        Map<String, Object> data = Map.of(CREDENTIAL_SUBJECT, credentialSubjectData);

        // Test the method
        Mono<String> result = service.generateVcPayLoad(templateContent, subjectDid, issuerDid, data, expiration);

        // Verify the output
        StepVerifier.create(result)
                .assertNext(response -> {
                    //assertEquals("ssdasd", response);
                    assertTrue(response.contains("zQ3shg2Mqz6NBj3afSySic9ynMrGk5Vgo9atHLXj4NWgxd7Xh"), "The response does not contain the expected substring.");
                    // Additional assertions as necessary
                })
                .verifyComplete();
    }

}