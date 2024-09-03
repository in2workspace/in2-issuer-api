package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CredentialDataTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConstructorAndGetters() {
        // Arrange
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("key", "value");

        // Act
        CredentialData credentialData = CredentialData.builder().payload(objectNode).build();

        // Assert
        assertEquals(objectNode, credentialData.payload());
    }

    @Test
    void testSetters() {
        // Arrange
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("newKey", "newValue");

        // Act
        CredentialData credentialData = CredentialData.builder()
                .payload(objectNode)
                .build();

        // Assert
        assertEquals(objectNode, credentialData.payload());
    }
}