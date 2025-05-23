package es.in2.issuer.backend.shared.domain.model.dto;

import es.in2.issuer.backend.shared.domain.model.enums.SignatureType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SignedDataTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        SignatureType expectedType = SignatureType.COSE;
        String expectedData = "sampleData";
        // Act
        SignedData signedData = new SignedData(expectedType, expectedData);
        // Assert
        assertEquals(expectedType, signedData.type());
        assertEquals(expectedData, signedData.data());
    }

    @Test
    void lombokGeneratedMethodsTest() {
        // Arrange
        SignatureType expectedType = SignatureType.COSE;
        String expectedData = "sampleData";
        // Act
        SignedData data1 = new SignedData(expectedType, expectedData);
        SignedData data2 = new SignedData(expectedType, expectedData);
        // Assert
        assertEquals(data1, data2); // Tests equals() method generated by Lombok
        assertEquals(data1.hashCode(), data2.hashCode()); // Tests hashCode() method generated by Lombok
    }

}