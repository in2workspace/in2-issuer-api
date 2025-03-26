package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.VerifiableCertification;
import es.in2.issuer.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.DID_ELSI;
import static es.in2.issuer.domain.util.Constants.SIGNATURE_REMOTE_TYPE_SERVER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VerifiableCertificationFactoryTest {
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DefaultSignerConfig defaultSignerConfig;
    @Mock
    private JWTService jwtService;
    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    @Mock
    private RemoteSignatureConfig remoteSignatureConfig;

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
        String token = "valid-token";
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_SERVER);
        // Given: A mocked JsonNode input representing the VerifiableCertification
        String credentialJson = """
                {
                  "@context": ["https://www.w3.org/2018/credentials/v1"],
                  "id": "urn:uuid:%s",
                  "type": ["VerifiableCertification"],
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
                      "hash": "1234",
                      "scope": "compliance-scope",
                      "standard": "compliance-standard"
                    }],
                    "product": {
                      "productId": "product-id",
                      "productName": "Product Name",
                      "productVersion": "1.0"
                    }
                  },
                  "validFrom": "2023-09-07T00:00:00Z",
                  "validUntil": "2024-09-07T00:00:00Z"
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
                .validFrom("2023-09-07T00:00:00Z")
                .validUntil("2024-09-07T00:00:00Z")
                .signer(VerifiableCertification.Signer.builder()
                        .commonName(defaultSignerConfig.getCommonName())
                        .country(defaultSignerConfig.getCountry())
                        .emailAddress(defaultSignerConfig.getEmail())
                        .organization(defaultSignerConfig.getOrganization())
                        .organizationIdentifier(defaultSignerConfig.getOrganizationIdentifier())
                        .serialNumber(defaultSignerConfig.getSerialNumber())
                        .build())
                .issuer(VerifiableCertification.Issuer.builder()
                        .commonName(defaultSignerConfig.getCommonName())
                        .country(defaultSignerConfig.getCountry())
                        .id(DID_ELSI + defaultSignerConfig.getOrganizationIdentifier())
                        .organization(defaultSignerConfig.getOrganization())
                        .build())
                .atester(VerifiableCertification.Atester.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .country("Country")
                        .id("did:key:1234")
                        .organization("Organization")
                        .organizationIdentifier("VATES-1234")
                        .build())
                .build();

        SignedJWT signedJWT = mock(SignedJWT.class);
        Payload jwtPayload = mock(Payload.class);
        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn("vcJson");
        LEARCredentialEmployee learCredential = getLEARCredentialEmployee();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee("vcJson")).thenReturn(learCredential);

        when(objectMapper.convertValue(credentialNode, VerifiableCertification.class)).thenReturn(verifiableCertification);

        when(objectMapper.writeValueAsString(any(VerifiableCertification.class))).thenReturn("expectedString");

        // When: Calling mapAndBuildVerifiableCertification
        Mono<CredentialProcedureCreationRequest> resultMono = verifiableCertificationFactory.mapAndBuildVerifiableCertification(credentialNode, token, "S");

        // Then: Verify the result
        StepVerifier.create(resultMono)
                .expectNextMatches(credentialProcedureCreationRequest -> credentialProcedureCreationRequest.credentialId() != null &&
                        credentialProcedureCreationRequest.organizationIdentifier().equals("VATES-D70795026") &&
                        credentialProcedureCreationRequest.credentialDecoded() != null)
                .verifyComplete();
    }

    // Auxiliary methods to create LEARCredentialEmployee objects
    private LEARCredentialEmployee getLEARCredentialEmployee() {
        Mandator mandator = Mandator.builder()
                .organizationIdentifier("VATES-1234")
                .organization("Organization")
                .country("Country")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("John")
                .lastName("Doe")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(mandator)
                .mandatee(mandatee)
                .build();
        LEARCredentialEmployee.CredentialSubject credentialSubject = LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
        return LEARCredentialEmployee.builder()
                .credentialSubject(credentialSubject)
                .build();
    }
}