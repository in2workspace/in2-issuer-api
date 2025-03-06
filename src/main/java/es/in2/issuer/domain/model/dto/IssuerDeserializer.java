package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class IssuerDeserializer extends JsonDeserializer<Issuer> {
    @Override
    public Issuer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        // If the node is a string, we convert it to SimpleIssuer.
        if (node.isTextual()) {
            return new SimpleIssuer(node.asText());
        }
        // If the node is an object, we convert it to DetailedIssuer.
        else if (node.isObject()) {
            return p.getCodec().treeToValue(node, DetailedIssuer.class);
        }
        throw new JsonMappingException(p, "Unexpected data type for Issuer");
    }
}

