package es.in2.issuer.backend.domain.service.impl;
import es.in2.issuer.backend.domain.exception.HashGenerationException;
import es.in2.issuer.backend.domain.service.HashGeneratorService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class HashGeneratorServiceImpl implements  HashGeneratorService {

    @Override
    public String generateHash(String unsignedDocument, String algorithm) throws HashGenerationException {
        if (algorithm == null || algorithm.isEmpty()) {
            throw new HashGenerationException("Algorithm is required");
        }

        if ("2.16.840.1.101.3.4.2.1".equals(algorithm)) {
            return generateSHA256(unsignedDocument);
        } else {
            throw new HashGenerationException("Error generating hash: algorithm not supported");
            }
    }
    @Override
    public String generateSHA256(String unsignedDocument) throws HashGenerationException {
        try {
            if (unsignedDocument == null || unsignedDocument.isEmpty()) {
                throw new HashGenerationException("The document cannot be null or empty");
            }

            byte[] documentBytes = unsignedDocument.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(documentBytes);

            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new HashGenerationException("SHA-256 algorithm not supported", e);
        }
    }
}
