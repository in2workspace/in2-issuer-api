package es.in2.issuer.api.model.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProofDTOTest {

    @Test
    public void testConstructorAndGetters() {
        // Arrange
        String expectedProofType = "jwt_vc_json";
        String expectedJwt = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiw....WZwmhmn9OQp6YxX0a2L";

        // Act
        ProofDTO proofDTO = new ProofDTO(expectedProofType, expectedJwt);

        // Assert
        assertEquals(expectedProofType, proofDTO.getProofType());
        assertEquals(expectedJwt, proofDTO.getJwt());
    }

    @Test
    public void lombokGeneratedMethodsTest() {
        // Arrange
        String expectedProofType = "jwt_vc_json";
        String expectedJwt = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiw....WZwmhmn9OQp6YxX0a2L";

        // Act
        ProofDTO dto1 = new ProofDTO(expectedProofType, expectedJwt);
        ProofDTO dto2 = new ProofDTO(expectedProofType, expectedJwt);

        // Assert
        assertEquals(dto1, dto2); // Tests equals() method generated by Lombok
        assertEquals(dto1.hashCode(), dto2.hashCode()); // Tests hashCode() method generated by Lombok
        assertEquals("ProofDTO(proofType=" + expectedProofType +
                ", jwt=" + expectedJwt + ")", dto1.toString()); // Tests toString() method generated by Lombok
    }
}
