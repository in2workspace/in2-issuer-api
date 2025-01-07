package es.in2.issuer.domain.model.dto;

import es.in2.issuer.domain.model.enums.CredentialType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialProcedureCreationRequestTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedCredentialId = "123456";
        String expectedOrganizationIdentifier = "org123";
        String expectedCredentialDecoded = "Decoded Credential";
        CredentialType expectedCredentialType = CredentialType.LEAR_CREDENTIAL_EMPLOYEE;
        String expectedSubject = "Subject";

        // Act
        CredentialProcedureCreationRequest request = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded,
                expectedCredentialType,
                expectedSubject
        );

        // Assert
        assertEquals(expectedCredentialId, request.credentialId());
        assertEquals(expectedOrganizationIdentifier, request.organizationIdentifier());
        assertEquals(expectedCredentialDecoded, request.credentialDecoded());
    }

    @Test
    void testSetters() {
        // Arrange
        String newCredentialId = "654321";
        String newOrganizationIdentifier = "org654";
        String newCredentialDecoded = "New Decoded Credential";

        // Act
        CredentialProcedureCreationRequest request = CredentialProcedureCreationRequest.builder()
                .credentialId(newCredentialId)
                .organizationIdentifier(newOrganizationIdentifier)
                .credentialDecoded(newCredentialDecoded)
                .build();

        // Assert
        assertEquals(newCredentialId, request.credentialId());
        assertEquals(newOrganizationIdentifier, request.organizationIdentifier());
        assertEquals(newCredentialDecoded, request.credentialDecoded());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedCredentialId = "123456";
        String expectedOrganizationIdentifier = "org123";
        String expectedCredentialDecoded = "Decoded Credential";
        CredentialType expectedCredentialType1 = CredentialType.LEAR_CREDENTIAL_EMPLOYEE;
        CredentialType expectedCredentialType2 = CredentialType.VERIFIABLE_CERTIFICATION;
        String expectedSubject = "Subject";

        CredentialProcedureCreationRequest request1 = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded,
                expectedCredentialType1,
                expectedSubject
        );
        CredentialProcedureCreationRequest request2 = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded,
                expectedCredentialType2,
                expectedSubject
        );

        // Assert
        assertEquals(expectedCredentialType1, request1.credentialType());
        assertEquals(expectedCredentialType2, request2.credentialType());
    }
}