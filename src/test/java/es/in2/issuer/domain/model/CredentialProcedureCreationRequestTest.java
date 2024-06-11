package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialProcedureCreationRequestTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedCredentialId = "123456";
        String expectedOrganizationIdentifier = "org123";
        String expectedCredentialDecoded = "Decoded Credential";

        // Act
        CredentialProcedureCreationRequest request = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded
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

        CredentialProcedureCreationRequest request1 = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded
        );
        CredentialProcedureCreationRequest request2 = new CredentialProcedureCreationRequest(
                expectedCredentialId,
                expectedOrganizationIdentifier,
                expectedCredentialDecoded
        );

        // Assert
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}