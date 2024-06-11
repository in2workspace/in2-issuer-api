package es.in2.issuer.domain.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.in2.issuer.domain.model.dto.LEARCredentialRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LEARCredentialRequestTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConstructorAndGetters() throws JsonProcessingException {
        // Arrange
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("key", "value");

        // Act
        LEARCredentialRequest learCredentialRequest = new LEARCredentialRequest(objectNode);

        // Assert
        assertEquals(objectNode, learCredentialRequest.credential());
    }

    @Test
    void testSetters() throws JsonProcessingException {
        // Arrange
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("newKey", "newValue");

        // Act
        LEARCredentialRequest learCredentialRequest = LEARCredentialRequest.builder()
                .credential(objectNode)
                .build();

        // Assert
        assertEquals(objectNode, learCredentialRequest.credential());
    }
}