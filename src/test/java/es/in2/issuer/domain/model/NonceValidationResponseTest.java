package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.dto.NonceValidationResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NonceValidationResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        Boolean expectedIsNonceValid = true;

        // Act
        NonceValidationResponse nonceValidationResponse = new NonceValidationResponse(expectedIsNonceValid);

        // Assert
        assertEquals(expectedIsNonceValid, nonceValidationResponse.isNonceValid());
    }

    @Test
    void testSetters() {
        // Arrange
        Boolean newIsNonceValid = false;

        // Act
        NonceValidationResponse nonceValidationResponse = NonceValidationResponse.builder()
                .isNonceValid(newIsNonceValid)
                .build();

        // Assert
        assertEquals(newIsNonceValid, nonceValidationResponse.isNonceValid());
    }
}