package es.in2.issuer.backend.shared.domain.model.dto;

import es.in2.issuer.backend.shared.domain.model.dto.Grant;
import es.in2.issuer.backend.shared.domain.model.dto.PreAuthorizedCodeResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreAuthorizedCodeResponseTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        Grant grant = new Grant("type", new Grant.TxCode(4, "numeric", "description"));
        String expectedPin = "1234";

        // Act
        PreAuthorizedCodeResponse preAuthorizedCodeResponse = new PreAuthorizedCodeResponse(
                grant,
                expectedPin
        );

        // Assert
        assertEquals(grant, preAuthorizedCodeResponse.grant());
        assertEquals(expectedPin, preAuthorizedCodeResponse.pin());
    }

    @Test
    void testSetters() {
        // Arrange
        Grant grant = new Grant("newType", new Grant.TxCode(5, "newNumeric", "newDescription"));
        String newPin = "5678";

        // Act
        PreAuthorizedCodeResponse preAuthorizedCodeResponse = PreAuthorizedCodeResponse.builder()
                .grant(grant)
                .pin(newPin)
                .build();

        // Assert
        assertEquals(grant, preAuthorizedCodeResponse.grant());
        assertEquals(newPin, preAuthorizedCodeResponse.pin());
    }
}