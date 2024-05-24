package es.in2.issuer.domain.model;

import es.in2.issuer.domain.model.dto.VerifiableCredentialJWT;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VerifiableCredentialJWTTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String expectedToken = "sampleToken";
        // Act
        VerifiableCredentialJWT jwt = new VerifiableCredentialJWT(expectedToken);
        // Assert
        assertEquals(expectedToken, jwt.token());
    }

    @Test
    void testSetters() {
        // Arrange
        VerifiableCredentialJWT jwt = VerifiableCredentialJWT.builder().token("newToken").build();
        // Assert
        assertEquals("newToken", jwt.token());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedToken = "sampleToken";
        // Act
        VerifiableCredentialJWT jwt1 = new VerifiableCredentialJWT(expectedToken);
        VerifiableCredentialJWT jwt2 = new VerifiableCredentialJWT(expectedToken);
        // Assert
        assertEquals(jwt1, jwt2); // Tests equals() method generated by Lombok
        assertEquals(jwt1.hashCode(), jwt2.hashCode()); // Tests hashCode() method generated by Lombok
    }

}
