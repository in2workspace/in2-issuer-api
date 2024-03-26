package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

import org.springframework.core.io.Resource;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;


class CredentialIssuerMetadataServiceImplTest {
    @Mock
    private GenericIamAdapter genericIamAdapter;

    @Mock
    private IamAdapterFactory iamAdapterFactory;
    @Mock
    private AppConfiguration appConfiguration;
    @InjectMocks
    private CredentialIssuerMetadataServiceImpl service;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(appConfiguration.getIssuerDomain()).thenReturn("https://test.issuer.com");
        String mockTokenUri = "https://token.uri";
        when(iamAdapterFactory.getAdapter()).thenReturn(genericIamAdapter);
        when(genericIamAdapter.getTokenUri()).thenReturn(mockTokenUri);

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
        ReflectionTestUtils.setField(service, "verifiableIdTemplate", mockResource);
    }

    @Test
    void testGenerateOpenIdCredentialIssuer() {
        StepVerifier.create(service.generateOpenIdCredentialIssuer())
                .assertNext(metadata -> {
                    Assertions.assertEquals("https://test.issuer.com", metadata.credentialIssuer());
                    Assertions.assertEquals("https://test.issuer.com/api/vc/credential", metadata.credentialEndpoint());
                    Assertions.assertNotNull(metadata.credentialsSupported());
                    Assertions.assertEquals(2, metadata.credentialsSupported().size());
                })
                .verifyComplete();
    }
}
