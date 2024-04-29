package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final ObjectMapper objectMapper;
    @Override
    public Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration) {
        return Mono.fromCallable(() -> {
            // Parse vcTemplate to a JsonNode
            JsonNode vcTemplateNode = objectMapper.readTree(vcTemplate);

            // Generate a unique UUID for jti and vc.id
            String uuid = "urn:uuid:" + UUID.randomUUID();

            // Calculate timestamps
            Instant nowInstant = Instant.now();
            long nowTimestamp = nowInstant.getEpochSecond();
            long expTimestamp = expiration.getEpochSecond();

            // Update vcTemplateNode with dynamic values
            ((ObjectNode) vcTemplateNode).put("id", uuid);
            ((ObjectNode) vcTemplateNode).put("issuer", issuerDid);
            // Update issuanceDate, issued, validFrom, expirationDate in vcTemplateNode using ISO 8601 format
            String nowDateStr = nowInstant.toString();
            String expirationDateStr = expiration.toString();
            ((ObjectNode) vcTemplateNode).put("issuanceDate", nowDateStr);
            ((ObjectNode) vcTemplateNode).put("validFrom", nowDateStr);
            ((ObjectNode) vcTemplateNode).put("expirationDate", expirationDateStr);

            // Convert userData to JsonNode and add the subjectDid
            JsonNode credentialSubjectValue = objectMapper.readTree(userData);
            ((ObjectNode) credentialSubjectValue).put("id", subjectDid);
            ((ObjectNode) vcTemplateNode).set("credentialSubject", credentialSubjectValue);

            // Construct final JSON ObjectNode
            ObjectNode finalObject = objectMapper.createObjectNode();
            finalObject.put("sub", subjectDid);
            finalObject.put("nbf", nowTimestamp);
            finalObject.put("iss", issuerDid);
            finalObject.put("exp", expTimestamp);
            finalObject.put("iat", nowTimestamp);
            finalObject.put("jti", uuid);
            finalObject.set("vc", vcTemplateNode);

            // Return final object as String
            return objectMapper.writeValueAsString(finalObject);
        });
    }
    @Override
    public Mono<String> generateDeferredVcPayLoad(String vcTemplate) {
        return Mono.fromCallable(() -> {
            // Parse vcTemplate to a JsonNode
            JsonNode vcTemplateNode = objectMapper.readTree(vcTemplate);
            String subjectDid = vcTemplateNode.get("credentialSubject").get("id").asText();
            String issuerDid = vcTemplateNode.get("issuer").asText();

            // Calculate timestamps
            String nowTimestamp = vcTemplateNode.get("validFrom").asText();
            String expTimestamp = vcTemplateNode.get("expirationDate").asText();


            // Construct final JSON ObjectNode
            ObjectNode finalObject = objectMapper.createObjectNode();
            finalObject.put("sub", subjectDid);
            finalObject.put("nbf", nowTimestamp);
            finalObject.put("iss", issuerDid);
            finalObject.put("exp", expTimestamp);
            finalObject.put("iat", nowTimestamp);
            finalObject.put("jti", subjectDid);
            finalObject.set("vc", vcTemplateNode);

            // Return final object as String
            return objectMapper.writeValueAsString(finalObject);
        });
    }
    @Override
    public Mono<String> generateVc(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration) {
        return Mono.fromCallable(() -> {
            // Parse vcTemplate to a JsonNode
            JsonNode vcTemplateNode = objectMapper.readTree(vcTemplate);

            // Generate a unique UUID for jti and vc.id
            String uuid = "urn:uuid:" + UUID.randomUUID();

            // Calculate timestamps
            Instant nowInstant = Instant.now();

            // Update vcTemplateNode with dynamic values
            ((ObjectNode) vcTemplateNode).put("id", uuid);
            ((ObjectNode) vcTemplateNode).put("issuer", issuerDid);
            // Update issuanceDate, issued, validFrom, expirationDate in vcTemplateNode using ISO 8601 format
            String nowDateStr = nowInstant.toString();
            String expirationDateStr = expiration.toString();
            ((ObjectNode) vcTemplateNode).put("issuanceDate", nowDateStr);
            ((ObjectNode) vcTemplateNode).put("validFrom", nowDateStr);
            ((ObjectNode) vcTemplateNode).put("expirationDate", expirationDateStr);

            // Convert userData to JsonNode and add the subjectDid
            JsonNode credentialSubjectValue = objectMapper.readTree(userData);
            ((ObjectNode) credentialSubjectValue).put("id", subjectDid);
            ((ObjectNode) vcTemplateNode).set("credentialSubject", credentialSubjectValue);

            // Return final object as String
            return objectMapper.writeValueAsString(vcTemplateNode);
        });
    }
}
