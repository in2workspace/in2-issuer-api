package es.in2.issuer.domain.model.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LEARCredentialEmployeeTest {

    @Mock
    private Issuer issuer;

    @Test
    void testBuilderAndGettersUsingMockIssuer() {
        // Build nested objects for the credentialSubject property
        LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan lifeSpan =
                LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                        .startDateTime("2024-01-01T00:00:00Z")
                        .endDateTime("2025-01-01T00:00:00Z")
                        .build();

        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee =
                LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                        .id("mandatee-id")
                        .email("mandatee@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .mobilePhone("+123456789")
                        .nationality("US")
                        .build();

        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator =
                LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                        .commonName("MandatorCommonName")
                        .country("US")
                        .emailAddress("mandator@example.com")
                        .organization("MandatorOrg")
                        .organizationIdentifier("Org123")
                        .serialNumber("SN123")
                        .build();

        LEARCredentialEmployee.CredentialSubject.Mandate.Power power =
                LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                        .id("power-id")
                        .action("action-value")
                        .domain("domain-value")
                        .function("function-value")
                        .type("type-value")
                        .build();

        LEARCredentialEmployee.CredentialSubject.Mandate.Signer signer =
                LEARCredentialEmployee.CredentialSubject.Mandate.Signer.builder()
                        .commonName("SignerCommonName")
                        .country("US")
                        .emailAddress("signer@example.com")
                        .organization("SignerOrg")
                        .organizationIdentifier("SignerOrg123")
                        .serialNumber("SignerSN123")
                        .build();

        LEARCredentialEmployee.CredentialSubject.Mandate mandate =
                LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                        .id("mandate-id")
                        .lifeSpan(lifeSpan)
                        .mandatee(mandatee)
                        .mandator(mandator)
                        .power(List.of(power))
                        .signer(signer)
                        .build();

        LEARCredentialEmployee.CredentialSubject credentialSubject =
                LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(mandate)
                        .build();

        when(issuer.getId()).thenReturn("issuer-mock-id");

        // Build the LEARCredentialEmployee instance using the mock Issuer
        LEARCredentialEmployee employee = LEARCredentialEmployee.builder()
                .context(List.of("https://www.w3.org/2018/credentials/v1"))
                .id("credential-id")
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .description("Test credential with mock issuer")
                .credentialSubject(credentialSubject)
                .issuer(issuer)
                .validFrom("2024-01-01T00:00:00Z")
                .validUntil("2025-01-01T00:00:00Z")
                .build();

        // Verify that the getters return the expected values
        assertEquals(List.of("https://www.w3.org/2018/credentials/v1"), employee.getContext());
        assertEquals("credential-id", employee.getId());
        assertEquals(List.of("VerifiableCredential", "LEARCredentialEmployee"), employee.getType());
        assertEquals("Test credential with mock issuer", employee.getDescription());
        assertEquals("issuer-mock-id", employee.getIssuer().getId());
        assertEquals("2024-01-01T00:00:00Z", employee.getValidFrom());
        assertEquals("2025-01-01T00:00:00Z", employee.getValidUntil());
        // Verify some nested fields
        assertEquals("mandate-id", employee.getCredentialSubject().mandate().id());
        assertEquals("John", employee.getCredentialSubject().mandate().mandatee().firstName());
        assertEquals("MandatorOrg", employee.getCredentialSubject().mandate().mandator().organization());
        assertEquals("action-value", employee.getCredentialSubject().mandate().power().get(0).action());
        assertEquals("SignerOrg123", employee.getCredentialSubject().mandate().signer().organizationIdentifier());
    }

    @Test
    void testEqualsAndHashCodeUsingSameIssuer() {
        // Build nested objects for the credentialSubject property
        LEARCredentialEmployee.CredentialSubject.Mandate mandate =
                LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                        .id("mandate-id")
                        .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                .startDateTime("2024-01-01T00:00:00Z")
                                .endDateTime("2025-01-01T00:00:00Z")
                                .build())
                        .mandatee(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                                .id("mandatee-id")
                                .email("mandatee@example.com")
                                .firstName("John")
                                .lastName("Doe")
                                .mobilePhone("+123456789")
                                .nationality("US")
                                .build())
                        .mandator(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                                .commonName("MandatorCommonName")
                                .country("US")
                                .emailAddress("mandator@example.com")
                                .organization("MandatorOrg")
                                .organizationIdentifier("Org123")
                                .serialNumber("SN123")
                                .build())
                        .power(List.of(LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                                .id("power-id")
                                .action("action-value")
                                .domain("domain-value")
                                .function("function-value")
                                .type("type-value")
                                .build()))
                        .signer(LEARCredentialEmployee.CredentialSubject.Mandate.Signer.builder()
                                .commonName("SignerCommonName")
                                .country("US")
                                .emailAddress("signer@example.com")
                                .organization("SignerOrg")
                                .organizationIdentifier("SignerOrg123")
                                .serialNumber("SignerSN123")
                                .build())
                        .build();

        LEARCredentialEmployee.CredentialSubject credentialSubject =
                LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(mandate)
                        .build();

        DetailedIssuer detailedIssuer = DetailedIssuer.builder()
                .id("issuer-id")
                .organizationIdentifier("Org123")
                .organization("IssuerOrg")
                .country("US")
                .commonName("IssuerCommonName")
                .emailAddress("example@example.com")
                .serialNumber("IssuerSN123")
                .build();

        LEARCredentialEmployee employee1 = LEARCredentialEmployee.builder()
                .context(List.of("https://www.w3.org/2018/credentials/v1"))
                .id("credential-id")
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .description("Test credential")
                .credentialSubject(credentialSubject)
                .issuer(detailedIssuer)
                .validFrom("2024-01-01T00:00:00Z")
                .validUntil("2025-01-01T00:00:00Z")
                .build();

        LEARCredentialEmployee employee2 = LEARCredentialEmployee.builder()
                .context(List.of("https://www.w3.org/2018/credentials/v1"))
                .id("credential-id")
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .description("Test credential")
                .credentialSubject(credentialSubject)
                .issuer(detailedIssuer)
                .validFrom("2024-01-01T00:00:00Z")
                .validUntil("2025-01-01T00:00:00Z")
                .build();

        // Using the same mock issuer, the objects should be equal
        assertEquals(employee1, employee2);
        assertEquals(employee1.hashCode(), employee2.hashCode());
    }
}
