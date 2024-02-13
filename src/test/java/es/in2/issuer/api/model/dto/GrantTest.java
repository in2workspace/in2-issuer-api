package es.in2.issuer.api.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class GrantTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        String expectedPreAuthorizedCode = "1234";
        boolean expectedUserPinRequired = true;

        // Act
        Grant grant = new Grant(expectedPreAuthorizedCode, expectedUserPinRequired);

        // Assert
        assertEquals(expectedPreAuthorizedCode, grant.getPreAuthorizedCode());
        assertEquals(expectedUserPinRequired, grant.isUserPinRequired());
    }

    @Test
    public void testSetters() {
        // Arrange
        Grant grant = new Grant();

        // Act
        grant.setPreAuthorizedCode("5678");
        grant.setUserPinRequired(false);

        // Assert
        assertEquals("5678", grant.getPreAuthorizedCode());
        assertFalse(grant.isUserPinRequired());
    }
    @Test
    public void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedPreAuthorizedCode = "1234";
        boolean expectedUserPinRequired = true;

        // Act
        Grant grant1 = new Grant(expectedPreAuthorizedCode, expectedUserPinRequired);
        Grant grant2 = new Grant(expectedPreAuthorizedCode, expectedUserPinRequired);

        // Assert
        assertEquals(grant1, grant2); // Tests equals() method generated by Lombok
        assertEquals(grant1.hashCode(), grant2.hashCode()); // Tests hashCode() method generated by Lombok
        assertEquals("Grant(preAuthorizedCode=" + expectedPreAuthorizedCode +
                ", userPinRequired=" + expectedUserPinRequired + ")", grant1.toString()); // Tests toString() method generated by Lombok
    }
}
