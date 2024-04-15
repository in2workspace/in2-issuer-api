package es.in2.issuer.domain.model;

import org.junit.jupiter.api.Test;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CredentialIssuerMetadataTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedCredentialIssuer = "sampleIssuer";
        String expectedCredentialEndpoint = "https://example.com/credential";
        String expectedCredentialToken = "sampleToken";
        String expectedBatchCredentialEndpoint = "sampleBatchEndpoint";

        // Act
        CredentialIssuerMetadata metadata = new CredentialIssuerMetadata(
                expectedCredentialIssuer,
                expectedCredentialEndpoint,
                expectedBatchCredentialEndpoint,
                expectedCredentialToken,
                null
        );
        // Assert
        assertEquals(expectedCredentialIssuer, metadata.credentialIssuer());
        assertEquals(expectedCredentialEndpoint, metadata.credentialEndpoint());
        assertEquals(expectedCredentialToken, metadata.credentialToken());
        assertNull(metadata.credentialConfigurationsSupported());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedCredentialIssuer = "sampleIssuer";
        String expectedCredentialEndpoint = "https://example.com/credential";
        String expectedCredentialToken = "sampleToken";
        String expectedBatchCredentialEndpoint = "sampleBatchEndpoint";
        // Act
        CredentialIssuerMetadata metadata1 = new CredentialIssuerMetadata(
                expectedCredentialIssuer,
                expectedCredentialEndpoint,
                expectedBatchCredentialEndpoint,
                expectedCredentialToken,
                null
        );
        CredentialIssuerMetadata metadata2 = new CredentialIssuerMetadata(
                expectedCredentialIssuer,
                expectedCredentialEndpoint,
                expectedBatchCredentialEndpoint,
                expectedCredentialToken,
                null
        );
        // Assert
        assertEquals(metadata1, metadata2); // Tests equals() method generated by Lombok
        assertEquals(metadata1.hashCode(), metadata2.hashCode()); // Tests hashCode() method generated by Lombok
    }

}
