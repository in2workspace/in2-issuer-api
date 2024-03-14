package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.IDEPCommitCredential;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IDEPCommitCredentialTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        UUID expectedCredentialId = UUID.randomUUID();
        String expectedGicarId = "sampleGicarId";
        Date expectedExpirationDate = new Date();
        // Act
        IDEPCommitCredential dto = new IDEPCommitCredential(expectedCredentialId, expectedGicarId, expectedExpirationDate);
        // Assert
        assertEquals(expectedCredentialId, dto.credentialId());
        assertEquals(expectedGicarId, dto.gicarId());
        assertEquals(expectedExpirationDate, dto.expirationDate());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        UUID expectedCredentialId = UUID.randomUUID();
        String expectedGicarId = "sampleGicarId";
        Date expectedExpirationDate = new Date();
        // Act
        IDEPCommitCredential dto1 = new IDEPCommitCredential(expectedCredentialId, expectedGicarId, expectedExpirationDate);
        IDEPCommitCredential dto2 = new IDEPCommitCredential(expectedCredentialId, expectedGicarId, expectedExpirationDate);
        // Assert
        assertEquals(dto1, dto2); // Tests equals() method generated by Lombok
        assertEquals(dto1.hashCode(), dto2.hashCode()); // Tests hashCode() method generated by Lombok
    }

}