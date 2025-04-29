package es.in2.issuer.backend.shared.domain.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreAuthorizedCodeResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        Grants grants = new Grants("type", new Grants.TxCode(4, "numeric", "description"));
        String expectedPin = "1234";

        // Act
        PreAuthorizedCodeResponse preAuthorizedCodeResponse = new PreAuthorizedCodeResponse(
                grants,
                expectedPin
        );

        // Assert
        assertEquals(grants, preAuthorizedCodeResponse.grants());
        assertEquals(expectedPin, preAuthorizedCodeResponse.pin());
    }

    @Test
    void testSetters() {
        // Arrange
        Grants grants = new Grants("newType", new Grants.TxCode(5, "newNumeric", "newDescription"));
        String newPin = "5678";

        // Act
        PreAuthorizedCodeResponse preAuthorizedCodeResponse = PreAuthorizedCodeResponse.builder()
                .grants(grants)
                .pin(newPin)
                .build();

        // Assert
        assertEquals(grants, preAuthorizedCodeResponse.grants());
        assertEquals(newPin, preAuthorizedCodeResponse.pin());
    }
}