package es.in2.issuer.domain.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreAuthCodeResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        Grant grant = new Grant("type", new Grant.TxCode(4, "numeric", "description"));
        String expectedPin = "1234";

        // Act
        PreAuthCodeResponse preAuthCodeResponse = new PreAuthCodeResponse(
                grant,
                expectedPin
        );

        // Assert
        assertEquals(grant, preAuthCodeResponse.grant());
        assertEquals(expectedPin, preAuthCodeResponse.pin());
    }

    @Test
    void testSetters() {
        // Arrange
        Grant grant = new Grant("newType", new Grant.TxCode(5, "newNumeric", "newDescription"));
        String newPin = "5678";

        // Act
        PreAuthCodeResponse preAuthCodeResponse = PreAuthCodeResponse.builder()
                .grant(grant)
                .pin(newPin)
                .build();

        // Assert
        assertEquals(grant, preAuthCodeResponse.grant());
        assertEquals(newPin, preAuthCodeResponse.pin());
    }
}