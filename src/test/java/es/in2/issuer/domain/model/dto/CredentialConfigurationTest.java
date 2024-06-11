package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.dto.CredentialConfiguration;
import es.in2.issuer.domain.model.dto.CredentialDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialConfigurationTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedFormat = "format";
        List<String> expectedCryptographicBindingMethodsSupported = List.of("method1", "method2");
        List<String> expectedCredentialSigningAlgValuesSupported = List.of("alg1", "alg2");
        CredentialDefinition expectedCredentialDefinition = new CredentialDefinition(List.of("type"));

        // Act
        CredentialConfiguration credentialConfiguration = new CredentialConfiguration(
                expectedFormat,
                expectedCryptographicBindingMethodsSupported,
                expectedCredentialSigningAlgValuesSupported,
                expectedCredentialDefinition
        );

        // Assert
        assertEquals(expectedFormat, credentialConfiguration.format());
        assertEquals(expectedCryptographicBindingMethodsSupported, credentialConfiguration.cryptographicBindingMethodsSupported());
        assertEquals(expectedCredentialSigningAlgValuesSupported, credentialConfiguration.credentialSigningAlgValuesSupported());
        assertEquals(expectedCredentialDefinition, credentialConfiguration.credentialDefinition());
    }

    @Test
    void testSetters() {
        // Arrange
        String newFormat = "newFormat";
        List<String> newCryptographicBindingMethodsSupported = List.of("newMethod1", "newMethod2");
        List<String> newCredentialSigningAlgValuesSupported = List.of("newAlg1", "newAlg2");
        CredentialDefinition newCredentialDefinition = new CredentialDefinition(List.of("type"));

        // Act
        CredentialConfiguration credentialConfiguration = CredentialConfiguration.builder()
                .format(newFormat)
                .cryptographicBindingMethodsSupported(newCryptographicBindingMethodsSupported)
                .credentialSigningAlgValuesSupported(newCredentialSigningAlgValuesSupported)
                .credentialDefinition(newCredentialDefinition)
                .build();

        // Assert
        assertEquals(newFormat, credentialConfiguration.format());
        assertEquals(newCryptographicBindingMethodsSupported, credentialConfiguration.cryptographicBindingMethodsSupported());
        assertEquals(newCredentialSigningAlgValuesSupported, credentialConfiguration.credentialSigningAlgValuesSupported());
        assertEquals(newCredentialDefinition, credentialConfiguration.credentialDefinition());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedFormat = "format";
        List<String> expectedCryptographicBindingMethodsSupported = List.of("method1", "method2");
        List<String> expectedCredentialSigningAlgValuesSupported = List.of("alg1", "alg2");
        CredentialDefinition expectedCredentialDefinition = new CredentialDefinition(List.of("type"));

        CredentialConfiguration config1 = new CredentialConfiguration(
                expectedFormat,
                expectedCryptographicBindingMethodsSupported,
                expectedCredentialSigningAlgValuesSupported,
                expectedCredentialDefinition
        );
        CredentialConfiguration config2 = new CredentialConfiguration(
                expectedFormat,
                expectedCryptographicBindingMethodsSupported,
                expectedCredentialSigningAlgValuesSupported,
                expectedCredentialDefinition
        );

        // Assert
        assertEquals(config1, config2);
        assertEquals(config1.hashCode(), config2.hashCode());
    }
}