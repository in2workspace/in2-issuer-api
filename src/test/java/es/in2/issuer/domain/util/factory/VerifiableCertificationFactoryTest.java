package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VerifiableCertificationFactoryTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DefaultSignerConfig defaultSignerConfig;

    @InjectMocks
    private VerifiableCertificationFactory verifiableCertificationFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(defaultSignerConfig.getCommonName()).thenReturn("Common Name");
        when(defaultSignerConfig.getCountry()).thenReturn("Country");
        when(defaultSignerConfig.getEmail()).thenReturn("email@example.com");
        when(defaultSignerConfig.getOrganization()).thenReturn("Organization");
        when(defaultSignerConfig.getOrganizationIdentifier()).thenReturn("OrgIdentifier");
        when(defaultSignerConfig.getSerialNumber()).thenReturn("SerialNumber");

    }

    @Test
    void testMapAndBuildVerifiableCertification() throws Exception {
        // Given: A mocked JsonNode input representing the VerifiableCertification
        String credentialJson = """
                {
                  "@context": ["https://www.w3.org/2018/credentials/v1"],
                  "id": "urn:uuid:%s",
                  "type": ["VerifiableCertification"],
                  "issuer": {
                    "commonName": "Issuer Common Name",
                    "country": "Issuer Country",
                    "id": "Issuer ID",
                    "organization": "Issuer Organization"
                  },
                  "credentialSubject": {
                    "company": {
                      "address": "1234 Example St.",
                      "commonName": "Company Name",
                      "country": "Country",
                      "email": "company@example.com",
                      "id": "company-id",
                      "organization": "Company Organization"
                    },
                    "compliance": [{
                      "id": "compliance-id",
                      "scope": "compliance-scope",
                      "standard": "compliance-standard"
                    }],
                    "product": {
                      "productId": "product-id",
                      "productName": "Product Name",
                      "productVersion": "1.0"
                    }
                  },
                  "issuanceDate": "2023-09-07T00:00:00Z",
                  "validFrom": "2023-09-07T00:00:00Z",
                  "expirationDate": "2024-09-07T00:00:00Z",
                  "signer": {
                    "commonName": "Signer Name",
                    "country": "Signer Country",
                    "emailAddress": "signer@example.com",
                    "organization": "Signer Organization",
                    "organizationIdentifier": "SignerOrgIdentifier",
                    "serialNumber": "SignerSerialNumber"
                  }
                }
                """.formatted(UUID.randomUUID().toString());

        JsonNode credentialNode = objectMapper.readTree(credentialJson);

        // Mock the objectMapper's convertValue method
        VerifiableCertification verifiableCertification = VerifiableCertification.builder()
                .context(List.of("https://www.w3.org/2018/credentials/v1"))
                .id("urn:uuid:" + UUID.randomUUID())
                .type(List.of("VerifiableCertification"))
                .issuer(VerifiableCertification.Issuer.builder()
                        .commonName("Issuer Common Name")
                        .country("Issuer Country")
                        .id("Issuer ID")
                        .organization("Issuer Organization")
                        .build())
                .credentialSubject(VerifiableCertification.CredentialSubject.builder()
                        .company(VerifiableCertification.CredentialSubject.Company.builder()
                                .address("1234 Example St.")
                                .commonName("Company Name")
                                .country("Country")
                                .email("company@example.com")
                                .id("company-id")
                                .organization("Company Organization")
                                .build())
                        .compliance(List.of(VerifiableCertification.CredentialSubject.Compliance.builder()
                                .id("compliance-id")
                                .scope("compliance-scope")
                                .standard("compliance-standard")
                                .build()))
                        .product(VerifiableCertification.CredentialSubject.Product.builder()
                                .productId("product-id")
                                .productName("Product Name")
                                .productVersion("1.0")
                                .build())
                        .build())
                .issuanceDate("2023-09-07T00:00:00Z")
                .validFrom("2023-09-07T00:00:00Z")
                .expirationDate("2024-09-07T00:00:00Z")
                .signer(VerifiableCertification.Signer.builder()
                        .commonName("Signer Name")
                        .country("Signer Country")
                        .emailAddress("signer@example.com")
                        .organization("Signer Organization")
                        .organizationIdentifier("SignerOrgIdentifier")
                        .serialNumber("SignerSerialNumber")
                        .build())
                .build();

        when(objectMapper.convertValue(credentialNode, VerifiableCertification.class)).thenReturn(verifiableCertification);

        when(objectMapper.writeValueAsString(any(VerifiableCertificationJwtPayload.class))).thenReturn("expectedString");

        // When: Calling mapAndBuildVerifiableCertification
        Mono<CredentialProcedureCreationRequest> resultMono = verifiableCertificationFactory.mapAndBuildVerifiableCertification(credentialNode);

        // Then: Verify the result
        StepVerifier.create(resultMono)
                .expectNextMatches(credentialProcedureCreationRequest -> credentialProcedureCreationRequest.credentialId() != null &&
                        credentialProcedureCreationRequest.organizationIdentifier().equals("OrgIdentifier") &&
                        credentialProcedureCreationRequest.credentialDecoded() != null)
                .verifyComplete();
    }
}