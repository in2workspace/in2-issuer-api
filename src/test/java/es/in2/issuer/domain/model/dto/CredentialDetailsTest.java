package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.domain.model.dto.CredentialDetails;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialDetailsTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConstructorAndGetters() {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String expectedCredentialStatus = "Valid";
        String expectedCredentialJson = "{\"key\": \"value\"}";
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(expectedCredentialJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Act
        CredentialDetails credentialDetails = new CredentialDetails(uuid, expectedCredentialStatus, jsonNode);

        // Assert
        assertEquals(uuid, credentialDetails.procedureId());
        assertEquals(expectedCredentialStatus, credentialDetails.credentialStatus());
        assertEquals(jsonNode, credentialDetails.credential());
    }

    @Test
    void testSetters() throws JsonProcessingException {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String newCredentialStatus = "Revoked";
        JsonNode jsonNode = objectMapper.readTree("{\"key\": \"value\"}");

        // Act
        CredentialDetails credentialDetails = CredentialDetails.builder()
                .procedureId(uuid)
                .credentialStatus(newCredentialStatus)
                .credential(jsonNode)
                .build();

        // Assert
        assertEquals(uuid, credentialDetails.procedureId());
        assertEquals(newCredentialStatus, credentialDetails.credentialStatus());
        assertEquals(jsonNode, credentialDetails.credential());
    }

    @Test
    void lombokGeneratedMethodsTest() throws JsonProcessingException {
        // Arrange
        UUID uuid = UUID.randomUUID();
        String expectedCredentialStatus = "Valid";
        JsonNode jsonNode = objectMapper.readTree("{\"key\": \"value\"}");

        CredentialDetails credentialDetails = new CredentialDetails(uuid, expectedCredentialStatus, jsonNode);
        CredentialDetails credentialDetails2 = new CredentialDetails(uuid, expectedCredentialStatus, jsonNode);

        // Assert
        assertEquals(credentialDetails, credentialDetails2);
        assertEquals(credentialDetails.hashCode(), credentialDetails2.hashCode());
    }
}